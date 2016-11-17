import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ludwighandel on 16-01-03.
 */
public class aWord{

    private Word theWord;
    public HashMap<Document, AttributeInfo> attributesinfo = new HashMap<Document, AttributeInfo>();


    public aWord(Word theWord,Attributes attributes){
        this.theWord = theWord;
    }

    public void update(Word theWord, Attributes attributes){

        // Check if the document exists
        if(attributesinfo.get(attributes.document)!=null){
            attributesinfo.get(attributes.document).count++;
        }
        else
        {
            attributesinfo.put(attributes.document,new AttributeInfo(attributes));
        }

    }


}

class AttributeInfo implements Comparable<AttributeInfo>{

    Document theDoc;
    int count = 1;     // how often do the search terms appear in a document
    int popularity;    // Higher is better
    public double relevance = 0.0;     // Higher is better

    public AttributeInfo(Attributes attributes){
        popularity = attributes.document.popularity;
        theDoc = attributes.document;
        relevance = 0.0;
    }

    public double getReference(String orderby)
    {
        if(orderby.equals("popularity")) {
            return (double)this.popularity;
        }else if(orderby.equals("relevance")) {
            return this.relevance;
        }else {
            return (double)this.count;
        }

    }

    public int compareTo(AttributeInfo that)
    {
        int res;
        Double one = new Double(this.relevance);
        Double two = new Double(that.relevance);
        res = one.compareTo(two);

        return res;
    }
}