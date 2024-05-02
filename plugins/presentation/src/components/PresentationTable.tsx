import React, { useEffect, useState } from 'react';
import { Table, TableColumn } from '@backstage/core-components';
import { useApi} from '@backstage/core-plugin-api';
import { catalogApiRef } from '@backstage/plugin-catalog-react';
import { Entity } from '@backstage/catalog-model';

type RowData = {
  name: string;
  title: string;
  event: string;
  date: string;
  presenter: string;
};

export const PresentationTable = () => {
  const catalogApi = useApi(catalogApiRef);
  const [presentations, setPresentations] = useState<RowData[]>([]);

  useEffect(() => {
    const fetchPresentations = async () => {
      const response = await catalogApi.getEntities({ filter: { kind: 'Presentation' } });
      const presentationData = response.items.map((entity: Entity) => ({
        name: entity.metadata.name,
        title: entity.spec?.title,
        presenter: entity.spec?.presenter,
        event: entity.spec?.event,
        date: entity.spec?.date,
      }));
      setPresentations(presentationData);
    };

    fetchPresentations();
  }, [catalogApi]);

  const columns: TableColumn[] = [
    { 
      title: 'Title', 
      field: 'title', 
      render: (rowData: RowData) => <a href={`/presentations/${rowData.name}`}>{rowData.title}</a>
    },
    { title: 'Presenter', field: 'presenter' },
    { title: 'Date', field: 'date' },
    { title: 'Event', field: 'event' },
  ];

  return <Table title="Presentations" columns={columns} data={presentations} />;
};

export default PresentationTable;
