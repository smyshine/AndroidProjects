package com.example.smy.xmltoexcel;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Created by SMY on 2016/11/2.
 */
public class XlsToAccess {
    HSSFSheet    globalSheet    = null;

    /*读取一个指定单元格内容*/
    public String readCellValue(String pos)
    {
        int xpos;
        short ypos;
        int cellType;  /*取得此单元格的类型 0-Numeric,1-String,3-null*/
        String result; /*返回取得的单元格的值*/

        ypos = (short) (pos.toUpperCase().charAt(0) - 65);
        xpos = Integer.parseInt(pos.substring(1, pos.length())) - 1;

        HSSFRow row = null;  /* 定义excel中的行 */
        HSSFCell cell = null;  /* 定义excel中的单元格 */

        /* 根据xPos和yPos取得单元格 */
        row = globalSheet.getRow(xpos);
        cell = row.getCell(ypos);
        /** **************此处如果是空需要修改********************************** */

        cellType = cell.getCellType();
        switch (cellType)
        {
            case 0: /* 0-Numeric */
                result = String.valueOf(cell.getNumericCellValue());
                break;
            case 1: /* 1-String */
                result = cell.getStringCellValue();
                break;
            case 3: /* 3-null */
                result = "";
                break;
            default:
                result = "";
                break;
        }

        return result;
    }

    /*读取excel文件并把内容插入到access表中*/
    public void insertIntoTable() throws Exception
    {
        // 创建对Excel工作簿文件的引用
        HSSFWorkbook workbook =
                new HSSFWorkbook(new FileInputStream("D:/temp/test.xls"));
        // 获得一个sheet
        globalSheet = workbook.getSheetAt(0);

        String value1 = readCellValue("c1");
        String value2 = readCellValue("c2");
        String value3 = readCellValue("c3");
        String value4 = readCellValue("c4");

        System.out.println(value1);
        System.out.println(value2);

        /* 插入数据库 */
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        String url = "jdbc:odbc:asima";

        Connection conn = DriverManager.getConnection(url);
        PreparedStatement stmt =
                conn.prepareStatement("insert into custom values(?,?,?,?)");
        // 定义查询的SQL语句
        stmt.setString(1, value1);
        stmt.setString(2, value2);
        stmt.setString(3, value3);
        stmt.setString(4, value4);
        stmt.executeUpdate();

        stmt.close(); // 关闭statement
        conn.close(); // 关闭连接
    }
}
