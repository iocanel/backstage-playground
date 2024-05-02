import {TemplateExample} from '@backstage/plugin-scaffolder-node';
import yaml from 'yaml';

export const examples: TemplateExample[] = [
    {
        description: 'Saves an API definition to a file',
        example: yaml.stringify({
            steps: [
                {
                    action: 'api:save',
                    id: 'api-save',
                    name: 'Save an API definition',
                    input: {
                        values: {
                            api: 'github.api',
                            targetPath: 'openapi.yaml',
                        },
                    },
                },
            ],
        }),
    },
];
