import {
  coreServices,
  createBackendPlugin,
} from '@backstage/backend-plugin-api';
import { createRouter } from './service/router';

/**
 * presentationBackendPlugin backend plugin
 *
 * @public
 */
export const presentationBackendPlugin = createBackendPlugin({
  pluginId: 'presentationBackendPlugin',
  register(env) {
    env.registerInit({
      deps: {
        httpRouter: coreServices.httpRouter,
        logger: coreServices.logger,
      },
      async init({
        httpRouter,
        logger,
      }) {
        httpRouter.use(
          await createRouter({
            logger,
          }),
        );
        httpRouter.addAuthPolicy({
          path: '/health',
          allow: 'unauthenticated',
        });
      },
    });
  },
});

import { CatalogBuilder } from '@backstage/plugin-catalog-backend';
import { PresentationEntitiesProcessor } from '@iocanel/plugin-presentation-backend';
import { Router } from 'express';
import { PluginEnvironment } from '../../../packages/backend/src/types';

export default async function createBackend(env: PluginEnvironment): Promise<Router> {
  const builder = CatalogBuilder.create(env);

  console.log('Adding presentation processor');
  builder.addProcessor(new PresentationEntitiesProcessor());

  const { processingEngine, router } = await builder.build();

  await processingEngine.start();

  return router;
}
