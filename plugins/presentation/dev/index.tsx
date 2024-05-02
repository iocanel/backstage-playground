import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { presentationPlugin, Presentation } from '../src/plugin';

createDevApp()
  .registerPlugin(presentationPlugin)
  .addPage({
    element: <Presentation />,
    title: 'Root Page',
    path: '/presentation',
  })
  .render();
