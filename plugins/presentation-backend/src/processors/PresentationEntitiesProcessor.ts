import {
  Entity,
  getCompoundEntityRef,
  parseEntityRef,
  RELATION_OWNED_BY,
  RELATION_OWNER_OF,
} from '@backstage/catalog-model';
import {
  CatalogProcessor,
  CatalogProcessorEmit,
  processingResult,
} from '@backstage/plugin-catalog-node';
import { LocationSpec } from '@backstage/plugin-catalog-common';
import {
  PresentationEntityV1beta1,
  presentationEntityV1beta1Validator,
} from '@iocanel/plugin-presentation-common';

/**
 * Adds support for scaffolder specific entity kinds to the catalog.
 *
 * @public
 */
export class PresentationEntitiesProcessor implements CatalogProcessor {
  getProcessorName(): string {
    return 'PresentationEntitiesProcessor';
  }

  private readonly validators = [presentationEntityV1beta1Validator];

  async validateEntityKind(entity: Entity): Promise<boolean> {
    for (const validator of this.validators) {
      if (await validator.check(entity)) {
        return true;
      }
    }

    return false;
  }

  async postProcessEntity(
    entity: Entity,
    _location: LocationSpec,
    emit: CatalogProcessorEmit,
  ): Promise<Entity> {
    const selfRef = getCompoundEntityRef(entity);

    if (
      entity.apiVersion === 'iocanel.com/v1beta1' &&
      entity.kind === 'Presentation'
    ) {
      const template = entity as PresentationEntityV1beta1;

      const target = template.spec.owner;
      if (target) {
        const targetRef = parseEntityRef(target, {
          defaultKind: 'Group',
          defaultNamespace: selfRef.namespace,
        });
        emit(
          processingResult.relation({
            source: selfRef,
            type: RELATION_OWNED_BY,
            target: {
              kind: targetRef.kind,
              namespace: targetRef.namespace,
              name: targetRef.name,
            },
          }),
        );
        emit(
          processingResult.relation({
            source: {
              kind: targetRef.kind,
              namespace: targetRef.namespace,
              name: targetRef.name,
            },
            type: RELATION_OWNER_OF,
            target: selfRef,
          }),
        );
      }
    }

    return entity;
  }
}
