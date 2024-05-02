import React from 'react';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import PresentationTable from './PresentationTable';

const PresentationsPage = () => {
  return (
    <Card>
      <CardContent>
      <PresentationTable />
    </CardContent>
    </Card>
  );
};

export default PresentationsPage;
