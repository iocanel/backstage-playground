import React, { useEffect, useRef, useState } from 'react';
import Reveal from 'reveal.js';
import { catalogApiRef } from '@backstage/plugin-catalog-react'
import { useApi } from '@backstage/core-plugin-api';

import 'reveal.js/dist/reveal.css';
import 'reveal.js/dist/theme/black.css';
import { PresentationEntityV1beta1 } from '@iocanel/plugin-presentation-common';
import { useParams } from 'react-router-dom';

const PresentationPage = () => {
  const revealRef = useRef(null);
  const { name } = useParams();
  const [presentation, setPresentation] = useState<PresentationEntityV1beta1 | undefined>(undefined);
  const [content, setContent] = useState<string | undefined>(undefined);
  const catalogApi = useApi(catalogApiRef);


  useEffect(() => {
    catalogApi.getEntityByName({kind: 'Presentation', name: name}).then((presentation: PresentationEntityV1beta1) => {
      setPresentation(presentation);
    });
  }, [name]);


  useEffect(() => {
    if (revealRef.current && presentation?.spec?.content) {
      const deck = new Reveal(revealRef.current);
      deck.initialize({
        hash: true,
        transition: 'slide',
        embedded: true,
        controls: true,
        progress: true,
        width: "100%",
        height: "100%",
      });
      setContent(presentation?.spec?.content);
    }
  }, [presentation]);

  return (
    <div className="reveal" ref={revealRef} style={{ width: '89vw', height: '100vh' }}>
      <div className="slides" dangerouslySetInnerHTML={{ __html: content }} />
    </div>
  );
};

export default PresentationPage;
