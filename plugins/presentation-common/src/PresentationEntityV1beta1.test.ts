import { entityKindSchemaValidator } from '@backstage/catalog-model';
import type { PresentationEntityV1beta1 } from './PresentationEntityV1beta1';
import schema from './Presentation.v1beta1.schema.json';

const validator = entityKindSchemaValidator(schema);

describe('presentationEntityV1beta1Validator', () => {
  let entity: PresentationEntityV1beta1;

  beforeEach(() => {
    entity = {
      apiVersion: 'iocanel.com/v1beta1',
      kind: 'Presentation',
      metadata: {
        name: 'example-presentation',
      },
      spec: {
        owner: 'team:example',
        title: 'The Future of Web Development',
        date: '2024-03-28T00:00:00.000Z',
        event: 'WebDev Conference 2024',
        presenter: 'Jane Doe',
        content: 'In this presentation, we will explore...'
      },
    };
  });

  it('happy path: accepts valid data', async () => {
    expect(validator(entity)).toBe(entity);
  });

  it('ignores unknown apiVersion', async () => {
    (entity as any).apiVersion = 'iocanel.com/v1beta0';
    expect(validator(entity)).toBe(false);
  });

  it('ignores unknown kind', async () => {
    (entity as any).kind = 'Talk';
    expect(validator(entity)).toBe(false);
  });

  it('accepts missing owner', async () => {
    delete (entity as any).spec.owner;
    expect(validator(entity)).toBe(entity);
  });

  it('rejects missing title', async () => {
    delete (entity.spec as any).title;
    expect(() => validator(entity)).toThrow(/title/);
  });

  it('rejects missing date', async () => {
    delete (entity.spec as any).date;
    expect(() => validator(entity)).toThrow(/date/);
  });

  it('rejects missing event', async () => {
    delete (entity.spec as any).event;
    expect(() => validator(entity)).toThrow(/event/);
  });

  it('rejects missing presenter', async () => {
    delete (entity.spec as any).presenter;
    expect(() => validator(entity)).toThrow(/presenter/);
  });

  it('rejects empty content', async () => {
    (entity.spec as any).content = '';
    expect(() => validator(entity)).toThrow(/content/);
  });
});
