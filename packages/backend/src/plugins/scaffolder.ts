import { CatalogClient } from '@backstage/catalog-client';
import { Router } from 'express';
import type { PluginEnvironment } from '../types';
import { ScmIntegrations } from '@backstage/integration';
import { createBuiltinActions, createRouter } from '@backstage/plugin-scaffolder-backend';
import { createQuarkusApp, cloneQuarkusQuickstart } from '@qshift/plugin-quarkus-backend';
import { createArgoCdResources } from '@roadiehq/scaffolder-backend-argocd'
import saveApi from './api';

export default async function createPlugin(
  env: PluginEnvironment,
): Promise<Router> {
  const catalogClient = new CatalogClient({
    discoveryApi: env.discovery,
  });
  const integrations = ScmIntegrations.fromConfig(env.config);

  const builtInActions = createBuiltinActions({
    integrations,
    catalogClient,
    config: env.config,
    reader: env.reader,
  });

  const actions = [
    createArgoCdResources( env.config, env.logger ),
    ...builtInActions,
    saveApi(catalogClient),
    createQuarkusApp(),
    cloneQuarkusQuickstart()
  ];

  return await createRouter({
    actions,
    logger: env.logger,
    config: env.config,
    database: env.database,
    reader: env.reader,
    catalogClient,
    identity: env.identity,
    permissions: env.permissions,
  });
}
