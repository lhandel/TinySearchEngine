
import se.kth.id1020.Driver;
import se.kth.id1020.TinySearchEngineBase;

/**
 * Created by ludwighandel on 16-01-03.
 */
public class main {

    public static void main(String[] args) throws Exception{
        TinySearchEngineBase searchEngine = new TinySearchEngine();
        Driver.run(searchEngine);
    }
}


/* Loop throue the words
         * Add the results to temp result
         * Loop
         *
         * intersection
mergeNonDuplicates
mergeNonDuplicates
intersection
intersection
         *
         * got 1 results in 0m 0s 82ms 733µs 451ns
         * Document{ck02, pop=453330457}
         *
         * + + | nightmare stone | metaphysical stuck + dark night orderby popularity desc
         * Query((((nightmare | stone) + (metaphysical | stuck)) + (dark + night)) ORDERBY POPULARITY DESC)
         */