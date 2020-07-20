package services;

import entities.FileEntity;
import entities.Steps;
import entities.Ticket;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static utils.UtilClass.getFirstKeyFromMap;
import static utils.UtilClass.getFirstValueFromMap;

@Component
public class FileManager {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ConfigManager configManager;

    private static final String PATH_TO_FILE = "target/test1.xls";

    private static HSSFCellStyle createStyleForTitle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static HSSFCellStyle createSpecialStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = createStyleForTitle(workbook);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        return style;
    }

    /**
     * create row for the Excel document
     * @param sheet
     * @param index
     * @param entity
     * @param workbook
     * @return
     */
    public Row createRow(HSSFSheet sheet, int index, FileEntity entity, HSSFWorkbook workbook) {
        boolean isPriceHigher = configManager.isPriceMoreThanMax(
                getFirstValueFromMap(entity.getTicket().getPositions()),
                entity.getMaxPrice());
        Row row = sheet.createRow(index);
        Cell cell;

        cell = row.createCell(0, CellType.STRING);
        cell.setCellValue(entity.getTicket().getId());
        sheet.setColumnWidth(0, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue(entity.getTicket().getTitle());
        sheet.setColumnWidth(1, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue(entity.getTicket().getRegion());
        sheet.setColumnWidth(2, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue(entity.getTicket().getAktdat());
        sheet.setColumnWidth(3, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue(entity.getTicket().getCustomer());
        sheet.setColumnWidth(4, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue(getFirstKeyFromMap(entity.getTicket().getPositions()));
        sheet.setColumnWidth(5, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue(getFirstValueFromMap(entity.getTicket().getPositions()));
        sheet.setColumnWidth(6, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue(entity.getTicket().getTotalSum());
        sheet.setColumnWidth(7, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(8, CellType.STRING);
        cell.setCellValue(entity.getTicket().getLink());
        sheet.setColumnWidth(8, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(9, CellType.STRING);
        cell.setCellValue(entity.getMaxPrice());
        sheet.setColumnWidth(9, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(10, CellType.STRING);
        cell.setCellValue(entity.getMinPrice());
        sheet.setColumnWidth(10, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        cell = row.createCell(11, CellType.STRING);
        cell.setCellValue(entity.getAvgPrice());
        sheet.setColumnWidth(11, 5000);
        checkPricesForStyle(isPriceHigher, cell, workbook);

        return row;
    }

    /**
     * create header for the excel document
     * @param sheet
     * @return
     */
    public Row createHeader(HSSFSheet sheet) {
        Row row = sheet.createRow(0);
        Cell cell;

        cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Id");
        sheet.setColumnWidth(0, 5000);

        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("Title");
        sheet.setColumnWidth(1, 5000);

        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("Region");
        sheet.setColumnWidth(2, 5000);

        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue("Date");
        sheet.setColumnWidth(3, 5000);

        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue("Customer");
        sheet.setColumnWidth(4, 5000);

        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue("Position's title");
        sheet.setColumnWidth(5, 5000);

        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue("Position's price");
        sheet.setColumnWidth(6, 5000);

        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue("Total sum of the ticket");
        sheet.setColumnWidth(7, 5000);

        cell = row.createCell(8, CellType.STRING);
        cell.setCellValue("Link to the ticket");
        sheet.setColumnWidth(8, 5000);

        cell = row.createCell(9, CellType.STRING);
        cell.setCellValue("Max price from the parsing");
        sheet.setColumnWidth(9, 5000);

        cell = row.createCell(10, CellType.STRING);
        cell.setCellValue("Min price from the parsing");
        sheet.setColumnWidth(10, 5000);

        cell = row.createCell(11, CellType.STRING);
        cell.setCellValue("Average price from the parsing");
        sheet.setColumnWidth(11, 5000);
        return row;
    }

    /**
     * Listener for printing file after parsing tickets and prices.
     * @param eventManager
     * @throws IOException
     */
    @EventListener(condition = "#eventManager.step.equals('CREATE_FILE')")
    public void printFile(EventManager eventManager) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test sheet");

        Row row;
        //
        HSSFCellStyle style = createStyleForTitle(workbook);

        row = createHeader(sheet);

        // Data
        int rownum = 1;
        for (FileEntity fileEntity : eventManager.getFileEntities()) {
            row = createRow(sheet, rownum, fileEntity, workbook);
            rownum++;
        }
        File file = new File(PATH_TO_FILE);
    //    File file = new File("C:/Users/agliy/IdeaProjects/fightCorruption1/target/test1.xls");
        file.getParentFile().mkdirs();

        FileOutputStream outFile = new FileOutputStream(file);
        workbook.write(outFile);
        eventManager = new EventManager(this, Steps.SEND_MAIL.toString());
        publisher.publishEvent(eventManager);
        System.out.println("Created file: " + file.getAbsolutePath());
    }

    private static FileEntity newFileEntity() {
        Ticket ticket = new Ticket("123");
        ticket.setTitle("title1");
        ticket.setRegion("reg");
        ticket.setAktdat("date");
        ticket.setCustomer("Customer");
        ticket.setTotalSum(123.0);
        ticket.setLink("Link1");

        return new FileEntity(ticket, 10.0, 5.0, 7.5);
    }

    /**
     * Highlight cells where price of ticket more than the max price some percentage
     * @param isPriceHigher
     * @param cell
     * @param workbook
     */
    private void checkPricesForStyle(boolean isPriceHigher, Cell cell, HSSFWorkbook workbook) {
        if (isPriceHigher) {
            CellStyle cs = workbook.createCellStyle();
            cs.setFillForegroundColor(IndexedColors.RED.getIndex());
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(cs);
        }
    }
}
