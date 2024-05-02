import {
  Entity,
  entityKindSchemaValidator,
  KindValidator,
} from '@backstage/catalog-model';
import { JsonObject } from '@backstage/types';
import schema from './Presentation.v1beta1.schema.json';

/**
 * Presentation catalog entity kind.
 *
 * @public
 */
export interface PresentationEntityV1beta1 extends Entity {
  /**
   * The apiVersion string of the Presentation.
   */
  apiVersion: 'iocanel.com/v1beta1';
  /**
   * The kind of the entity.
   */
  kind: 'Presentation';
  /**
   * The specification of the Presentation Entity.
   */
  spec: {
    owner?: string;
    title: string;
    date: string; // ISO 8601 date format
    event: string;
    presenter: string;
    content: string; // Markdown content
  };
}

/**
 * Depends on how you designed your task you might tailor the behaviour for each of them.
 *
 * @public
 */
export interface PresentationRecoveryV1beta1 extends JsonObject {
  /**
   *
   * none - not recover, let the task be marked as failed
   * startOver - do recover, start the execution of the task from the first step.
   *
   * @public
   */
  EXPERIMENTAL_strategy?: 'none' | 'startOver';
}

/**
 * The presentation of the template.
 *
 * @public
 */
export interface PresentationPresentationV1beta1 extends JsonObject {
  /**
   * Overrides default buttons' text
   */
  buttonLabels?: {
    /**
     * The text for the button which leads to the previous template page
     */
    backButtonText?: string;
    /**
     * The text for the button which starts the execution of the template
     */
    createButtonText?: string;
    /**
     * The text for the button which opens template's review/summary
     */
    reviewButtonText?: string;
  };
}

/**
 * Step that is part of a Presentation Entity.
 *
 * @public
 */
export interface PresentationEntityStepV1beta1 extends JsonObject {
  id?: string;
  name?: string;
  action: string;
  input?: JsonObject;
  if?: string | boolean;
  'backstage:permissions'?: PresentationPermissionsV1beta1;
}

/**
 * Parameter that is part of a Presentation Entity.
 *
 * @public
 */
export interface PresentationParametersV1beta1 extends JsonObject {
  'backstage:permissions'?: PresentationPermissionsV1beta1;
}

/**
 *  Access control properties for parts of a template.
 *
 * @public
 */
export interface PresentationPermissionsV1beta1 extends JsonObject {
  tags?: string[];
}


const validator = entityKindSchemaValidator(schema);

/**
 * Entity data validator for {@link PresentationEntityV1beta1}.
 *
 * @public
 */
export const presentationEntityV1beta1Validator: KindValidator = {
  async check(data: Entity) {
    return validator(data) === data;
  },
};

/**
 * Typeguard for filtering entities and ensuring v1alpha1 entities.
 * @public
 */
export const isPresentationEntityV1beta1 = (
  entity: Entity,
): entity is PresentationEntityV1beta1 =>
  entity.apiVersion === 'iocanel.com/v1beta1' && 
  entity.kind === 'Presentation';
