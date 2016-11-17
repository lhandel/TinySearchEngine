import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.*;

/**
 * Created by ludwighandel on 16-01-03.
 */
public class TinySearchEngine implements TinySearchEngineBase {

    private HashMap<Integer, aWord> theIndex = new HashMap<Integer, aWord>();
    private HashMap<Document, Integer> allDocs = new HashMap<Document, Integer>();

    private String infix_string;

    public TinySearchEngine(){
    }

    public void preInserts() {

    }

    public void insert(Sentence sentence, Attributes attributes) {

        for(Word w: sentence.getWords()) {

            int hashCode = w.word.hashCode();

            if(allDocs.get(attributes.document)==null)
                allDocs.put(attributes.document,1);
            else
                allDocs.put(attributes.document, allDocs.get(attributes.document)+1);


            if(theIndex.get(hashCode)!=null)
            {
                // update attributes
                theIndex.get(hashCode).update(w, attributes);
            }
            else
            {
                theIndex.put(hashCode,new aWord(w,attributes));
            }

        }

    }

    public void postInserts() {

    }

    public List<Document> search(String s) {

        // Get the word
        String[] words = s.split(" ");

        int right = 0; // Words to remove from the right
        String orderby,direction;
        // Check asc/desc
        if(words[words.length-1].equals("asc")) {
            direction = "asc";
            right++;
        }else if(words[words.length-1].equals("desc")) {
            direction = "desc";
            right++;
        }else {
            direction = "asc"; // asc as default
        }

        // Is orderby set?
        if(words.length>(right+2) && words[words.length-(right+2)].equals("orderby"))
        {
            orderby = words[words.length-(right+1)];
            right=right+2; // delete "orderby" + the order word
        }
        else
        {
            orderby = "popularity"; // orderby popularity by default
        }


        // Used for infix
        StringBuilder infix = new StringBuilder("Query(");
        Stack<String> infixStack = new Stack<String>();

        Stack<ArrayList<AttributeInfo>> resultStack = new Stack<ArrayList<AttributeInfo>>();
        ArrayList<AttributeInfo>documents1 = new ArrayList<AttributeInfo>();
        ArrayList<AttributeInfo>documents2 = new ArrayList<AttributeInfo>();

        for(int i=words.length-right-1; i>=0; i--)
        {
                String word = words[i];

                // Is it and operation?
                if (word.equals("+") || word.equals("|") || word.equals("-"))
                {
                    documents1=resultStack.pop();
                    documents2=resultStack.pop();

                    if (word.equals("+"))
                        documents1 = intersection(documents1, documents2);

                    else if (word.equals("-"))
                        documents1 = getDifference(documents1, documents2);

                    else if (word.equals("|"))
                        documents1 = getUnion(documents1, documents2);

                    // add to resultstack
                    resultStack.push(documents1);

                    // Add to intfix
                    infixStack.push("("+infixStack.pop()+" "+word+" "+infixStack.pop()+")");
                }
                else
                {
                    // add to resultstack
                    ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(theIndex.get(word.hashCode()).attributesinfo.values());
                    for (AttributeInfo temp:list) {

                        double tf = (double)temp.count/(double)allDocs.get(temp.theDoc);
                        double idf = Math.log10((double)allDocs.size()/(double)list.size());
                        double rel = tf*idf;
                        temp.relevance = rel;
                    }
                    resultStack.push(list);

                    // Add to intfix
                    infixStack.push(word);
                }



        }

        // Done with all words and operation, get the end result
        ArrayList<AttributeInfo> result = new ArrayList<AttributeInfo>(resultStack.pop());
        infix.append(infixStack.pop());
        infix.append(" ORDERBY "+orderby.toUpperCase()+" "+direction.toUpperCase());
        this.infix_string = infix.toString();


        // sort if needed
        if(result.size()>1)
            bubblesort(result, orderby, direction);


        // Add to return list
        List<Document> endresult = new ArrayList<Document>();
        for (AttributeInfo temp:result) {
            endresult.add(temp.theDoc);
        }

        return endresult;
    }


    private ArrayList<AttributeInfo> getUnion(ArrayList<AttributeInfo> docsAtt, ArrayList<AttributeInfo> docsAtt2){

        HashMap<Document, AttributeInfo> theDocs = new HashMap<Document, AttributeInfo>();

        for (AttributeInfo temp:docsAtt2) {
            theDocs.put(temp.theDoc,temp);
        }

        for (AttributeInfo temp:docsAtt) {

            // if it exists, update the relevance
            if(theDocs.get(temp.theDoc)!=null)
            {
                double tempRel = theDocs.get(temp.theDoc).relevance;
                temp.relevance = temp.relevance + tempRel;
                theDocs.put(temp.theDoc,temp);
            }
            else // If not exists, add it
            {
                theDocs.put(temp.theDoc,temp);
            }

        }

        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(theDocs.values());
        return list;
    }

    private ArrayList<AttributeInfo> intersection(ArrayList<AttributeInfo> docsAtt, ArrayList<AttributeInfo> docsAtt2){

        HashMap<Document, AttributeInfo> theDocs = new HashMap<Document, AttributeInfo>();

        ArrayList<AttributeInfo>returnResult= new ArrayList<AttributeInfo>();

        for (AttributeInfo temp:docsAtt) {
            theDocs.put(temp.theDoc,temp);
        }

        for (AttributeInfo temp:docsAtt2) {

            if(theDocs.containsKey(temp.theDoc))
            {
                temp.relevance = temp.relevance + theDocs.get(temp.theDoc).relevance;
                returnResult.add(temp);
            }

        }
        return returnResult;
    }

    private ArrayList<AttributeInfo> getDifference(ArrayList<AttributeInfo> docsAtt, ArrayList<AttributeInfo> docsAtt2){

        ArrayList<AttributeInfo>returnResult= new ArrayList<AttributeInfo>();

        ArrayList<Document>docs=new ArrayList<Document>();

        for (AttributeInfo temp:docsAtt2) {
            Document tempDoc=temp.theDoc;
            docs.add(tempDoc);
        }

        for (AttributeInfo temp:docsAtt) {
            Document tempDoc=temp.theDoc;
            if(!docs.contains(tempDoc))
                returnResult.add(temp);
        }

        return returnResult;
    }

    public ArrayList bubblesort(ArrayList<AttributeInfo> a, String orderby, String direction){

        // Used to compare
        Comparable first, second;
        int current, next;

        if(a.isEmpty())
            throw new IllegalArgumentException("N needs to be larger than 0");

        int R = a.size()-2;
        boolean swapped = true;

        while(R>=0 && swapped==true)
        {
            swapped = false;
            for(int i=0; i<=R; i++)
            {
                // decreasing order or increasing order?
                if(direction.equals("desc")){
                    current = i;
                    next = i+1;
                }else{
                    current = i+1;
                    next = i;
                }

                first = a.get(current).getReference(orderby);
                second = a.get(next).getReference(orderby);

                if(first.compareTo(second) > 0){
                    swapped = true;
                  //  System.out.println(first+" - "+second);
                    Collections.swap(a,current,next);
                }
            }
            R = R-1;
        }
        return a;
    }

    public String infix(String s) {
        return this.infix_string;
    }
}

