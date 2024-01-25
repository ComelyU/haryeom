import { GetServerSideProps } from 'next';
import { useEffect, useState } from 'react';
import { StaticImageData } from 'next/image';
import styled from 'styled-components';
import PdfViewer from '@/components/PdfViewer';
import PaintCanvas from '@/components/PaintCanvas';
import HomeworkLayout from '@/components/layouts/HomeworkLayout';
import { getHomework } from '@/apis/homework/get-homework';
import { IHomework } from '@/apis/homework/homework';
import usePdf from '@/hooks/usePdf';
import useMyPaint from '@/components/PaintCanvas/Hook/useMyPaint';

interface HomeworkContainerProps {
    homeworkData: IHomework;
}

interface IMyImageDrawingImages {
    [pageNum: number]: string | StaticImageData;
}

const initMyDrawingImages = (homework: IHomework): IMyImageDrawingImages => {
    const { startPage, endPage } = homework;
    const drawingImages: IMyImageDrawingImages = {};
    for (let pageNum = startPage; pageNum <= endPage; pageNum++) {
        const drawingForPage = homework.drawings.find((drawing) => drawing.page === pageNum);
        if (drawingForPage) {
            drawingImages[pageNum] = drawingForPage.homeworkDrawingUrl;
        } else {
            drawingImages[pageNum] = '';
        }
    }
    return drawingImages;
};

const HomeworkContainer = ({ homeworkData }: HomeworkContainerProps) => {
    const [myDrawingImages, setMyDrawingImages] = useState<IMyImageDrawingImages>({});
    const {
        pdfPagesNum,
        pdfPageSize,
        pageNum,
        onDocumentLoadSuccess,
        onPageLoadSuccess,
        movePage,
        updatePdfPageSize,
    } = usePdf({ defaultPageNum: homeworkData.startPage });
    const { save } = useMyPaint({ updateImageSource: setMyDrawingImages });

    useEffect(() => {
        setMyDrawingImages(initMyDrawingImages(homeworkData));
    }, []);

    return (
        <HomeworkLayout>
            <StyledHomeworkContainer>
                <Board>
                    <PdfViewer
                        pdfUrl={homeworkData.textbook.textbookUrl}
                        pageNum={pageNum}
                        movePage={movePage}
                        pdfPagesNum={pdfPagesNum}
                        pdfPageSize={pdfPageSize}
                        onDocumentLoadSuccess={onDocumentLoadSuccess}
                        onPageLoadSuccess={onPageLoadSuccess}
                        updatePdfPageSize={updatePdfPageSize}
                    >
                        <DrawingLayer>
                            <PaintCanvas
                                imageSource={myDrawingImages[pageNum]}
                                save={save}
                                pdfPageSize={pdfPageSize}
                                pageNum={pageNum}
                            />
                        </DrawingLayer>
                    </PdfViewer>
                </Board>
            </StyledHomeworkContainer>
        </HomeworkLayout>
    );
};

export const getServerSideProps: GetServerSideProps = async (context) => {
    const homeworkId = context.params?.id as string;

    if (!homeworkId) return { props: {} };

    const homeworkData = await getHomework(homeworkId);

    return { props: { homeworkData } };
};

const StyledHomeworkContainer = styled.div`
    background-color: white;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
`;

const Board = styled.div`
    position: relative;
    width: 93%;
    flex: 0.93;
    overflow: auto;
    display: flex;
`;

const DrawingLayer = styled.div`
    position: absolute;
    top: 0;
    height: 0;
    width: 100%;
    height: 100%;
`;

export default HomeworkContainer;
