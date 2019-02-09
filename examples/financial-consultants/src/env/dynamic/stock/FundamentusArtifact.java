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

import cartago.Artifact;
import cartago.OPERATION;

public class FundamentusArtifact extends Artifact {

    @OPERATION
    void getFundamentals(String stock) {
        URL url;
        try {
            url = new URL("http://www.fundamentus.com.br/detalhes.php?papel=" + stock);
            log("Buscando fundamentos " + url.toString());
            Document doc = (Document) Jsoup.parse(url, 3000);

            Element table;
            Elements rows;
            Element row;
            Elements cols;
            Number number;
            String tmp;
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            if (doc.select("table").size() > 0) {
                                
                // Go to 1st table to get Current Price
                table = ((Element) doc).select("table").get(0);
                rows = table.select("tr");
                row = rows.get(0);
                cols = row.select("td");
                number = format.parse(cols.get(3).text());
                signal("setPreco", number.doubleValue());

                // Go to 2nd table to get Market Value
                table = ((Element) doc).select("table").get(1);
                rows = table.select("tr");
                row = rows.get(0);
                cols = row.select("td");
                tmp = cols.get(1).text().replaceAll("\\.", "");
                number = format.parse(tmp);
                signal("setValorMercado", number.doubleValue());

                // Go to 3rd table to get "Div. Yield" and "Div Br/ Patrim" = "D/E Debt over Equity"
                table = ((Element) doc).select("table").get(2);
                rows = table.select("tr");
                row = rows.get(8);
                cols = row.select("td");
                number = format.parse(cols.get(3).text());
                signal("setDivYield", number.doubleValue());
                rows = table.select("tr");
                row = rows.get(1);
                cols = row.select("td");
                number = format.parse(cols.get(5).text());
                signal("setLPA", number.doubleValue());
                rows = table.select("tr");
                row = rows.get(2);
                cols = row.select("td");
                number = format.parse(cols.get(5).text());
                signal("setVPA", number.doubleValue());
                rows = table.select("tr");
                row = rows.get(7);
                cols = row.select("td");
                number = format.parse(cols.get(5).text());
                signal("setROIC", number.doubleValue());
                rows = table.select("tr");
                row = rows.get(10);
                cols = row.select("td");
                tmp = cols.get(5).text().replaceAll("\\.", "");
                number = format.parse(tmp);
                signal("setDivBrutaPatr", number.doubleValue());

                // Go to 4th table to get Market Value
                table = ((Element) doc).select("table").get(3);
                rows = table.select("tr");
                row = rows.get(2);
                cols = row.select("td");
                tmp = cols.get(3).text().replaceAll("\\.", "");
                number = format.parse(tmp);
                signal("setDividaLiq", number.doubleValue());
                
                // Go to 5th table to get Market Value
                table = ((Element) doc).select("table").get(4);
                rows = table.select("tr");
                row = rows.get(3);
                cols = row.select("td");
                tmp = cols.get(1).text().replaceAll("\\.", "");
                number = format.parse(tmp);
                signal("setEBIT", number.doubleValue());
                
                log("Consulta ao site fundamentus.com.br feita com sucesso!");
            } else {
                log("Erro ao obter fundamentos!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
