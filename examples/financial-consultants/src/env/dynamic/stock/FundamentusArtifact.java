package dynamic.stock;

import java.net.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.lang.mutable.MutableDouble;

import cartago.Artifact;
import cartago.OPERATION;
import jason.architecture.AgArch;

public class FundamentusArtifact extends Artifact {

    @OPERATION
    void getFundamentals(String stock) {
        URL url;
        try {
            url = new URL("http://www.fundamentus.com.br/detalhes.php?papel=" + stock.toString());
            log(getCurrentOpAgentBody().getName()+" buscando fundamentos de " + url.toString());
            Document doc = (Document) Jsoup.parse(url, 3000);

            Element table;
            Elements rows;
            Element row;
            Elements cols;
            MutableDouble number = new MutableDouble(0.0);
            if (doc.select("table").size() > 0) {
                                
                // Go to 1st table to get Current Price
                table = ((Element) doc).select("table").get(0);
                rows = table.select("tr");
                row = rows.get(0);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(3).text(), number));
                signal("setPreco", stock, number.doubleValue());

                // Go to 2nd table to get Market Value
                table = ((Element) doc).select("table").get(1);
                rows = table.select("tr");
                row = rows.get(0);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(1).text(), number));
                signal("setValorMercado", stock, number.doubleValue());

                // Go to 3rd table to get "Div. Yield" and "Div Br/ Patrim" = "D/E Debt over Equity"
                table = ((Element) doc).select("table").get(2);
                rows = table.select("tr");
                row = rows.get(8);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(3).text(), number));
                signal("setDivYield", stock, number.doubleValue());

                rows = table.select("tr");
                row = rows.get(1);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(5).text(), number));
                signal("setLPA", stock, number.doubleValue());
                
                rows = table.select("tr");
                row = rows.get(2);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(5).text(), number));
                signal("setVPA", stock, number.doubleValue());
                
                rows = table.select("tr");
                row = rows.get(7);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(5).text(), number));
                signal("setROIC", stock, number.doubleValue());
                
                rows = table.select("tr");
                row = rows.get(10);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(5).text(), number));
                signal("setDivBrutaPatr", stock, number.doubleValue());

                // Go to 4th table to get Market Value
                table = ((Element) doc).select("table").get(3);
                rows = table.select("tr");
                row = rows.get(2);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(3).text(), number));
                signal("setDividaLiq", stock, number.doubleValue());
                
                // Go to 5th table to get Market Value
                table = ((Element) doc).select("table").get(4);
                rows = table.select("tr");
                row = rows.get(3);
                cols = row.select("td");
                if (isNumericThisBRLStr(cols.get(1).text(), number));
                signal("setEBIT", stock, number.doubleValue());
                
                log("Consulta ao site fundamentus.com.br realizada com sucesso por "+getCurrentOpAgentBody().getName()+"!");
            } else {
                log("Erro ao obter fundamentos!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isNumericThisBRLStr(String str, MutableDouble number) {
        try {
            str = str.replaceAll("\\.", "").replaceAll("%", "").replaceAll(",", "\\.");
            double n = Double.parseDouble(str); //Test if is parseble
            number.setValue(n);
        } catch (NumberFormatException nfe) {
            number.setValue(0.0);
            return false;
        }
        return true;
    }
}
