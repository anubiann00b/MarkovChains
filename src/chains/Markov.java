package chains;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Markov {
    
    File f;
    Map<Pair, Map<String, Integer>> chain;
    
    Markov(String file) {
        f = new File(file);
    }
    
    Markov(Document d) throws IOException {
        f = new File("mos.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        Elements es = d.select("a[href]");
        for (Element e : es) {
            String url = e.attr("abs:href");
            Document page;
            try {
                page = Jsoup.connect(url).get();
                System.out.println(url);
            } catch (Exception ex) {
                System.out.println("Failed to get " + url + ": " + ex);
                continue;
            }
            try {
                String data = page.getElementsByClass("lyricbox").html();
                System.out.println(data);
                out.write(data);
            } catch (NullPointerException ex) {
                System.out.println(ex);
            }
        }
        out.flush();
        out.close();
    }
    
    void initialize() throws IOException {
        chain = new HashMap<Pair, Map<String, Integer>>();
        
        BufferedReader r = new BufferedReader(new FileReader(f));
        
        String s1;
        String s2 = next(r);
        String s3 = next(r);
        
        String next;
        while ((next=next(r)) != null) {
            s1 = s2;
            s2 = s3;
            s3 = next;
            
            Pair current = new Pair(s1, s2);
            Map<String, Integer> vals = chain.get(current);
            if (vals == null)
                vals = new HashMap<String, Integer>();
            
            Integer i = vals.get(s3);
            if (i==null)
                i = 0;
            vals.put(s3, i+1);
            
            chain.put(current, vals);
        }
        
        r.close();
    }
    
    String next(BufferedReader r) throws IOException {
        String ret = "";
        int i;
        char c;
        
        while ((i = r.read()) != ' ') {
            if (i == -1)
                return null;
            c = (char) i;
            ret += c;
        }
        
        return ret;
    }
    
    String generate() {
        String text = "";
        String s1;
        String s2;
        
        Map<String, Integer> current = null;
            
        do {
            s1 = ((Map.Entry<Pair, Map<String, Integer>>) chain.entrySet()
                    .toArray()[(int)(Math.random()*chain.size())]).getKey().str1;
        
            s2 = ((Map.Entry<Pair, Map<String, Integer>>) chain.entrySet()
                    .toArray()[(int)(Math.random()*chain.size())]).getKey().str1;
            current = chain.get(new Pair(s2, s1));
        } while (current == null);
        
        for(int i=0;i<100;i++) {
            text += s2 + " ";
            
            int sum = 0;
            for (Map.Entry<String, Integer> e : current.entrySet())
                sum += e.getValue();
            
            int target = (int)(Math.random()*sum);
            String next = null;
            
            for (Map.Entry<String, Integer> e : current.entrySet()) {
                target -= e.getValue();
                if (target < 0) {
                    next = e.getKey();
                    break;
                }
            }
            
            s2 = s1;
            s1 = next;
            
            current = chain.get(new Pair(s2, s1));
        }
        
        return text;
    }
    
    public static void main(String[] args) throws IOException {
        //Markov m = new Markov("text.txt");
        Markov m = new Markov(Jsoup.connect("http://lyrics.wikia.com/Miracle_Of_Sound").get());
        m.initialize();
        System.out.println(m.generate());
    }
}

class Pair {
    
    String str1;
    String str2;
    
    Pair(String s1, String s2) {
        str1 = s1;
        str2 = s2;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Pair))
            return false;
        
        Pair p = (Pair) o;
        
        return str1.equals(p.str1) && str2.equals(p.str2);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(str1);
        hash = 67 * hash + Objects.hashCode(str2);
        return hash;
    }
}
