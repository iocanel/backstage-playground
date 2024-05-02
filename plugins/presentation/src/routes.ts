import { createRouteRef, createSubRouteRef } from '@backstage/core-plugin-api';

export const presentationsRoute = createRouteRef({
  id: 'presentations',
});

export const presentationRoute = createSubRouteRef({
  id: 'presentation',
  parent: presentationsRoute,
  path: '/:name',
});

