import {
  createPlugin,
  createRoutableExtension,
} from '@backstage/core-plugin-api';

import { presentationsRoute, presentationRoute } from './routes';

export const presentationPlugin = createPlugin({
  id: 'presentation',
  routes: {
    root: presentationsRoute,
    presentations: presentationRoute,
  },
});

export const PresentationsPage = presentationPlugin.provide(
  createRoutableExtension({
    name: 'Presentations',
    component: () =>
      import('./components/PresentationsPage').then(m => m.default),
    mountPoint: presentationsRoute,
  }),
);

export const PresentationPage = presentationPlugin.provide(
  createRoutableExtension({
    name: 'Presentation',
    component: () =>
      import('./components/PresentationPage').then(m => m.default),
    mountPoint: presentationRoute,
  }),
);
