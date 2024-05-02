import {createTemplateAction} from '@backstage/plugin-scaffolder-node';
import fs from 'fs-extra';
import path from 'path';
import {resolveSafeChildPath} from '@backstage/backend-common';
import axios from 'axios';
import JSZip from 'jszip';
import {examples} from "./api.example";
import { CatalogApi, CatalogClient } from '@backstage/catalog-client';

export const saveApi = (catalogClient: CatalogClient) => {
  return createTemplateAction<{ apiRef: string; targetPath: string, values: any }>({
    id: 'api:save',
    description: 'Saves an API defintion to a file',
    examples,
    schema: {
      input: {
        type: 'object',
        properties: {
          apiRef: {
            title: 'apiRef',
            description: 'The ref to the API',
            type: 'string'
          },
          targetPath: {
            title: 'targetPath',
            description: 'The targetPath under the workspace where the api will be saved',
            type: 'string'
          },
        },
      },
    },

    async handler(ctx) {
      console.log('Saving API: ' + ctx.input.values.apiRef + ' to ' + ctx.input.values.targetPath);
      const dest = resolveSafeChildPath(ctx.workspacePath, ctx.input.values.targetPath);
      const entity = await catalogClient.getEntityByRef(ctx.input.values.apiRef);
      //get the definition from the api spec
      const definition = entity?.spec?.definition;
      fs.promises.mkdir(path.dirname(dest), {recursive: true}).then(() => {
        fs.writeFileSync(dest, definition);
      });
    }
  });
};
export default saveApi;
