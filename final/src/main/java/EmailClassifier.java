import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class EmailClassifier {
    private FilteredClassifier model;
    private Instances structure;
    private StringToWordVector filter;
    private ArrayList < Attribute > dataAttribute;
    private Instances data;
    //private NaiveBayes model;

    
    public EmailClassifier() throws Exception {
        Attribute text = new Attribute("text", (List < String > ) null);

        ArrayList < String > spamClass = new ArrayList < > ();
        spamClass.add("spam");
        spamClass.add("ham");
        Attribute labelAttribute = new Attribute("label", spamClass);
        
        dataAttribute = new ArrayList < > ();
        dataAttribute.add(labelAttribute);
        dataAttribute.add(text);
       
        data = loadRawDataset("final/train.txt");
        saveArff(data, "final/train.arff");
        
        filter = new StringToWordVector();
        filter.setAttributeIndices("last");

        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(1);
        //use delimeter
        tokenizer.setDelimiters("\\W");
        filter.setTokenizer(tokenizer);
        filter.setLowerCaseTokens(true);
     
        model = new FilteredClassifier();

        // // build NaiveBayes 
        model.setClassifier(new NaiveBayesMultinomial());
        model.setFilter(filter);
        model.buildClassifier(data);
        
        structure = new Instances("EmailData", dataAttribute, 0);
        structure.setClassIndex(0); 
        
   
    }

    
    public Instances loadRawDataset(String filename) throws Exception{
     
        Instances dataset = new Instances("spam", dataAttribute, 10);

        dataset.setClassIndex(0);

        BufferedReader br = new BufferedReader(new FileReader(filename)); 
        for (String line;
            (line = br.readLine()) != null;) {
            // split at first occurance of n no. of words
            String[] parts = line.split("\\s+", 2);

            if (!parts[0].isEmpty() && !parts[1].isEmpty()) {

                DenseInstance row = new DenseInstance(2);
                row.setValue(dataAttribute.get(0), parts[0]);
                row.setValue(dataAttribute.get(1), parts[1]);

                dataset.add(row);
            }

        }
        br.close();
        return dataset;

    }


    public void saveArff(Instances dataset, String filename) throws Exception  {
        ArffSaver arffSaverInstance = new ArffSaver();
        arffSaverInstance.setInstances(dataset);
        arffSaverInstance.setFile(new File(filename));
        arffSaverInstance.writeBatch();
    }

    

    public boolean isSpam(String emailContent) throws Exception {
        Instance instance = new DenseInstance(structure.numAttributes());
        instance.setDataset(structure);
        instance.setValue(structure.attribute("text"), emailContent);

        double result = model.classifyInstance(instance);
        // spam if result == 0
        return result == 0.0;
    }

    
    //debug
    // public static void main(String[] args) {
    //     try {
    //         EmailClassifier model = new EmailClassifier();
            
    //         String email1 = "Dear Sir/Madam, You have won a prize! Click here to claim it.";
    //         String email2 = "Hey John, how are you? Let's catch up soon. Best, Alice";
            
    //         System.out.println("Email 1 is " + (model.isSpam(email1) ));
    //         System.out.println("Email 2 is " + (model.isSpam(email2) ));
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    
}