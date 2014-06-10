package eu.leads.processor.plugins.sentiment;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.PrintStream;
import java.util.*;

//Entity extraction imports
//

//Entity extraction imports

//

public class SentimentAnalysisModule implements SentimentAnalysis {

  static Properties props;
  static StanfordCoreNLP pipeline;
  static String serializedClassifier;
  static AbstractSequenceClassifier<CoreLabel> classifier;

  public SentimentAnalysisModule(String serializedClassifier) {
    initialize(serializedClassifier);
  }

  public static void initialize(String classifierName) {
    props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
    props.setProperty("parse.maxlen", "20"); //Comment this line to remove sentence size limit. Currently ignoring
    props.setProperty("tokenize.options", "untokenizable=noneDelete");
    // sentences with more than 20 words.
    pipeline = new StanfordCoreNLP(props);
    serializedClassifier = classifierName;
    classifier = CRFClassifier
                         .getClassifierNoExceptions(serializedClassifier);
  }

  /**
   * Reads an annotation from the given filename using the requested input.
   */
  public static Annotation getAnnotation(Input inputFormat, String text,
                                         boolean filterUnknown) {
    switch ( inputFormat ) {
      case TEXT: {
        Annotation annotation = new Annotation(text);
        return annotation;
      }
      default:
        throw new IllegalArgumentException("Unknown format " + inputFormat);
    }
  }

  @Override
  public Sentiment getOverallSentiment(String text) {
    boolean filterUnknown = false;

    List<Output> outputFormats = Arrays
                                         .asList(new Output[]{Output.ROOT});
    Input inputFormat = Input.TEXT;


    if ( text == null ) {
      System.out.println("No text provided");
      System.exit(-1);
    }


    Annotation annotation = pipeline.process(text);// getAnnotation(inputFormat, text, filterUnknown);
    //pipeline.annotate(annotation);

    Sentiment s = new Sentiment();
    s.value = 0;
    for ( CoreMap sentence : annotation
                                     .get(CoreAnnotations.SentencesAnnotation.class) ) {
      // System.out.print(sentence + " --> ");
      s.value += outputTree(null, sentence, outputFormats);
    }

    // System.out.print("The final sentiment is ");
    if ( s.value > 0 )
      s.tag = "Positive";
    else if ( s.value < 0 )
      s.tag = "Negative";
    else
      s.tag = "Neutral";

    // System.out.print("Sentiment value:" + s.value);

    return s;
  }

  /**
   * Outputs a tree using the output style requested
   */

  static double outputTree(PrintStream out, CoreMap sentence,
                           List<Output> outputFormats) {
    double r = 0;
    for ( Output output : outputFormats ) {
      switch ( output ) {
        case ROOT: {
          if ( sentence.get(SentimentCoreAnnotations.ClassName.class)
                       .equalsIgnoreCase("Very Positive") ) {
            // out.println("2");
            r = 2;
          } else if ( sentence.get(
                                          SentimentCoreAnnotations.ClassName.class)
                              .equalsIgnoreCase("Positive") ) {
            // out.println("1");
            r = 1;
          } else if ( sentence.get(
                                          SentimentCoreAnnotations.ClassName.class)
                              .equalsIgnoreCase("Negative") ) {
            // out.println("-1");
            r = -1;
          } else if ( sentence.get(
                                          SentimentCoreAnnotations.ClassName.class)
                              .equalsIgnoreCase("Very Negative") ) {
            // out.println("-2");
            r = -2;
          } else {
            // out.println("0");

          }
          break;
        }
        default:
          throw new IllegalArgumentException("Unknown output format "
                                                     + output);
      }
    }
    return r;
  }

  @Override
  public Sentiment getSentimentForEntity(String targetEntity,
                                         String text) {
    boolean filterUnknown = false;

    List<Output> outputFormats = Arrays
                                         .asList(new Output[]{Output.ROOT});
    Input inputFormat = Input.TEXT;

    if ( text == null ) {
      System.out.println("No text provided");
      System.exit(-1);
    }
    Annotation annotation = pipeline.process(text);// getAnnotation(inputFormat, text, filterUnknown);
//		pipeline.annotate(annotation);

    int tempCount = 3, i = 0; // also include tempCount sentences after the
    // entity is found
    CoreMap s1 = null, s2 = null, s3 = null;
    Sentiment s = new Sentiment();
    s.value = 0;
    for ( CoreMap sentence : annotation
                                     .get(CoreAnnotations.SentencesAnnotation.class) ) {
      if ( i == 0 ) {
        s1 = sentence;
      } else if ( i == 1 ) {
        s2 = sentence;
      } else if ( i == 2 ) {
        s3 = sentence;
      } else {
        s1 = s2;
        s2 = s3;
        s3 = sentence;
      }

      if ( sentence.toString().toUpperCase()
                   .contains(targetEntity.toUpperCase()) ) {

        if ( tempCount == 3 ) {
          if ( s1 != null ) {
            // System.out.print(s1 + " --> ");
            s.value += outputTree(System.out, s1, outputFormats);
          }
          if ( s2 != null ) {
            // System.out.print(s2 + " --> ");
            s.value += outputTree(System.out, s2, outputFormats);
          }
          if ( s3 != null ) {
            // System.out.print(s3 + " --> ");
            s.value += outputTree(System.out, s3, outputFormats);
          }
        }
        // System.out.print(sentence + " --> ");
        s.value += outputTree(System.out, sentence, outputFormats);

        tempCount = 2;
      } else if ( tempCount >= 0 && tempCount < 3 ) {
        tempCount--;
        // System.out.print(sentence + " --> ");
        s.value += outputTree(System.out, sentence, outputFormats);
      } else
        tempCount = 3;
      i++;
    }

    // System.out.print("The final sentiment is ");
    if ( s.value > 0 )
      s.tag = "Positive";
    else if ( s.value < 0 )
      s.tag = "Negative";
    else
      s.tag = "Neutral";

    // System.out.print("Sentiment value:" + s.value);
    return s;
  }

  @Override
  public Set<Entity> getEntities(String text) {

    Set<Entity> entities = new HashSet<Entity>();
    List<List<CoreLabel>> out = classifier.classify(text);
    for ( List<CoreLabel> sentence : out ) {
      for ( CoreLabel word : sentence ) {
        if ( !word.get(CoreAnnotations.AnswerAnnotation.class)
                  .equalsIgnoreCase("O") ) {
          Entity e = new Entity();
          e.name = word.word();
          e.type = word.get(CoreAnnotations.AnswerAnnotation.class);
          entities.add(e);
        }
      }
    }
    return entities;
  }

  static enum Output {
    PENNTREES, VECTORS, ROOT, PROBABILITIES
  }

  static enum Input {
    TEXT, TREES
  }

}
