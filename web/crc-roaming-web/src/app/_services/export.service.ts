import { Injectable } from '@angular/core';
import * as FileSaver from 'file-saver';
import * as html2pdf from 'html2pdf.js'
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf'
import 'jspdf-autotable'

const EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
const EXCEL_EXTENSION = '.xlsx';
const PDF_EXTENSION = '.pdf';
const BORDER = 15;

@Injectable()
export class ExportService {

    constructor() { }

    public exportAsExcelFile(data: any[], heading: string[][], filterBody: any[], filterHeading: string[][], excelFileName: string): void {
        const myworksheet = XLSX.utils.book_new();
        
        let filter = [];
        filter.push([]);
        filter[0].push(filterHeading[0][0]['content']);
        for (let i = 0; i < filterHeading[1].length; i++) {
            filter.push([]);
            filter[1 + i].push(filterHeading[1][i]);
            filter[1 + i].push(filterBody[0][i]);
        }

        let filterStart = heading[0].length + 1;
        let merge = {s: {r: 0, c: filterStart}, e: {r: 0, c: filterStart + 1}};

        let ws = XLSX.utils.sheet_add_aoa(myworksheet, filter, {origin:{r:0, c:filterStart}});
        if(!ws['!merges']) ws['!merges'] = [];
        ws['!merges'].push(merge);

        XLSX.utils.sheet_add_aoa(myworksheet, heading, {});
        XLSX.utils.sheet_add_json(myworksheet, data, { origin: 'A2', skipHeader: true });
        const myworkbook: XLSX.WorkBook = { Sheets: { 'data': myworksheet }, SheetNames: ['data'] };
        const excelBuffer: any = XLSX.write(myworkbook, { bookType: 'xlsx', type: 'array' });
        this.saveAsExcelFile(excelBuffer, excelFileName);
    }

    private saveAsExcelFile(buffer: any, fileName: string): void {
        const data: Blob = new Blob([buffer], { type: EXCEL_TYPE });
        FileSaver.saveAs(data, fileName + EXCEL_EXTENSION);
    }

    exportAsPdfFile(body: any[], heading: string[][], filterBody: any[], filterHeading: string[][], fileName: string) {
        let pdf = new jsPDF();

        let pageW = pdf.internal.pageSize.getWidth();
        this.addHeading(pdf, pageW, filterHeading, filterBody, fileName);

        pdf.setTextColor(99);
        (pdf as any).autoTable({
            startY: (pdf as any).lastAutoTable.finalY + 10,
            head: heading,
            body: body,
            styles: { font: 'OpenSans' },
            theme: 'striped',
        })

        pdf.save(fileName + PDF_EXTENSION)
    }

    exportChartsAsPDF(ids: string[], filterHeading: string[][], filterBody: any[], fileName: string) {
        let pdf = this.createPdf("portrait");

        let pageW = pdf.internal.pageSize.getWidth();
        this.addHeading(pdf, pageW, filterHeading, filterBody, fileName);

        let graphicsW;
        let graphicsH
        let imgStartY;
        let imgStartX;
        let imgSpace = 15;
        if (ids.length == 1) {
            graphicsW = pageW * 2;
            graphicsH = graphicsW * 0.33
            imgStartY = (pdf as any).lastAutoTable.finalY + 30;
        } else if (ids.length == 2) {
            graphicsW = pageW;
            graphicsH = graphicsW * 0.5
            imgStartY = (pdf as any).lastAutoTable.finalY + 10;
        } else if (ids.length == 3) {
            graphicsW = pageW * 0.75;
            graphicsH = graphicsW * 0.5
            imgStartY = (pdf as any).lastAutoTable.finalY + 2;
            imgSpace = 7;
        }
        imgStartX = pageW / 2 - graphicsW / 2;

        ids.forEach(id => {
            let img = this.getImgFromCanvas(id);
            pdf.addImage(img, 'PNG', imgStartX, imgStartY, graphicsW, graphicsH);
            imgStartY += graphicsH + imgSpace;
        });

        pdf.save(fileName + PDF_EXTENSION);
    }

    exportSingleLineChartAsPDF(id: string, filterHeading: string[][], filterBody: any[], fileName: string) {
        let pdf = this.createPdf("landscape");

        let pageW = pdf.internal.pageSize.getWidth();
        this.addHeading(pdf, pageW, filterHeading, filterBody, fileName);

        let graphicsW = pageW - BORDER * 2;
        let graphicsH = graphicsW * 0.5
        let imgStartY = (pdf as any).lastAutoTable.finalY + 10;
        let imgStartX = pageW / 2 - graphicsW / 2;

        let img = this.getImgFromCanvas(id);
        pdf.addImage(img, 'PNG', imgStartX, imgStartY, graphicsW, graphicsH);

        pdf.save(fileName + PDF_EXTENSION);
    }

    // exportSinglePieChartAsPDF(id: string, filterHeading: string[][], filterBody: any[], fileName: string) {
    //     let pdf = this.createPdf("portrait");

    //     let pageW = pdf.internal.pageSize.getWidth();
    //     this.addHeading(pdf, pageW, filterHeading, filterBody, fileName);

    //     let graphicsW = pageW * 2;
    //     let graphicsH = graphicsW * 0.33
    //     let imgStartY = (pdf as any).lastAutoTable.finalY + 30;
    //     let imgStartX = pageW / 2 - graphicsW / 2;

    //     let img = this.getImgFromCanvas(id);
    //     pdf.addImage(img, 'PNG', imgStartX, imgStartY, graphicsW, graphicsH);

    //     pdf.save(fileName + PDF_EXTENSION);
    // }

    private createPdf(orientation: "portrait" | "landscape"): jsPDF {
        return new jsPDF({
            format: 'a4',
            orientation: orientation,
            unit: 'mm'
        });
    }

    private getImgFromCanvas(id: string) {
        return (document.getElementById(id) as any).toDataURL('image/png');
    }

    private addHeading(pdf: jsPDF, pageW: number, filterHeading: string[][], filterBody: any[], fileName: string) {
        pdf.setFont('OpenSans');
        pdf.setFontSize(15);
        pdf.text(fileName, pageW / 2, BORDER, { align: 'center' });
        
        pdf.setTextColor(99);
        (pdf as any).autoTable({
            startY: BORDER + 3,
            head: filterHeading,
            body: filterBody,
            styles: { font: 'OpenSans' },
            theme: 'striped',
        })
    }

}