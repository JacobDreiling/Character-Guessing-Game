import java.lang.reflect.Array;
import java.util.*;
import java.util.Scanner;
import java.math.*;
import java.io.*;
import java.util.stream.IntStream;


public class BackEnd {
    public static int n = 0;
    public static int[][] score_table = {
            {2,1,0,-2},
            {1,1,0,-1},
            {0,0,0,0},
            {-2,-1,0,2}
    };
    public static ArrayList<ArrayList<String>> answers = new ArrayList<ArrayList<String>>();
    public static ArrayList<String> usedQs = new ArrayList<String>();
    public static ArrayList<String> names = new ArrayList<String>();
    public static ArrayList<Integer> scores = new ArrayList<Integer>();
    public static ArrayList<Integer> questionsIndex = new ArrayList<Integer>();
    public static String[] validResponses = {"Yes","No","Maybe","I don't know",};
    public static ArrayList<String> questions = new ArrayList<String>();
    public static int indexBestQ = -1;

    public static void main(String[] args) throws IOException {
        initialize();
        Scanner in = new Scanner(System.in);
        for(int i=0; i<20; i++){
            System.out.println(getNextQuestion());
            update(in.nextLine());
        }
        String guess = getBestChar();
        System.out.println("Is "+guess+" your character?");
        if(in.nextLine().equals("yes"))
            System.out.println("Nice I win");
        else
            System.out.println("Dang I lost");
    }
    
    public static void initialize() throws IOException {
        // initializing the game
        // here I am creating an ArrayList for the questions
        List<List<String>> table = new ArrayList<>();
        // here I am reading from the csv file
        BufferedReader br = new BufferedReader((new FileReader("src/CharacterInfoTableUPDATED.csv")));{
            String COMMA_DELIMITER = ",";
            String line;
            // here I am adding value to the table array, im getting the values from the csv file
            while((line = br.readLine()) != null){
                String[] values = line.split(COMMA_DELIMITER,-1);
                table.add(Arrays.asList(values));
            }
        }

        // here I am creating an ArrayList for the questions and getting the value from the table


        for(int i = 1; i < table.get(0).size(); i++){
            questions.add(table.get(0).get(i));
            questionsIndex.add(i-1);
        }

         n = 0;

        int line = 0;


      for(int i =1; i < table.size(); i++){
          n++;
          names.add(table.get(i).get(0));
          scores.add(0);
          ArrayList<String> new_answers = new ArrayList<String>();
          for(int j = 1; j < table.get(i).size(); j++){
              String value = table.get(i).get(j);
              if(!value.equals("Yes") && !value.equals("No") && !value.equals("Maybe") && !value.equals("I don't know")){
                  System.out.println("'"+value+ "' is not a valid response at row " +i+ ", column " +j);
                  new_answers.add("I don't know");
              }
              else{
                  new_answers.add(value);
              }
          }
        answers.add(new_answers);
      }
    }




        // here is the sigmoid method
        public static double sig(int x){
            return 1 / (1 + (n-1)*Math.exp(-x));
        }

        public static ArrayList<Double> probs(ArrayList<Integer> nums){
        int total = 0;
        ArrayList<Double> P = new ArrayList<Double>();
        for(int n: nums){
            double s = sig(n);
            P.add(s);
            total += s;
            }
        for(int x =0; x<P.size(); x++){
            P.set(x,P.get(x)/total);
        }
        return P;
        }

        public static double entropy(ArrayList<Double> nums){
        double sum = 0;
        for(int i = 0; i < nums.size(); i++){
            double val = nums.get(i);
            sum += val*Math.log(val)/Math.log(2);
        }
        return -sum;
        }

        public static int scoreTable(String s1, String s2){
        int i1 = 0;
        switch(s1){
            case "Yes":
                i1 = 0;
                break;
            case "Maybe":
                i1 = 1;
                break;
            case "I don't know":
                i1 = 2;
                break;
            default:
                i1 =3;
        }
            int i2 = 0;
            switch(s2){
                case "Yes":
                    i2 = 0;
                    break;
                case "Maybe":
                    i2 = 1;
                    break;
                case "I don't know":
                    i2 = 2;
                    break;
                default:
                    i2 =3;
            }
        return score_table[i1][i2];
        }


        //
        public static ArrayList<Integer> updateScores(ArrayList<Integer> nums, String answer, int q){
            ArrayList<Integer> newNums =  new ArrayList<Integer>();
            for(int i = 0; i< n; i++){
                String correctAns = answers.get(i).get(q);
                int val = scoreTable(answer,correctAns);
                newNums.add(nums.get(i)+val);

            }
            return newNums;
        }

        public static String getNextQuestion(){
        int qIndex = -1;
        double accuracy = -1.0;
        String text = "";
            for(int i = 0; i < questionsIndex.size(); i++){
            int currentQ = questionsIndex.get(i);
            String currentQTxt = questions.get(currentQ);
            double avgEnt = 0.0;
                for(String response: validResponses){
                    ArrayList<Double> p = probs(updateScores(scores,response,currentQ));
                    double probAnswer = 0.0;
                        for(int j = 0; j < n; j++){
                            if(response.equals(answers.get(j).get(currentQ))){
                                probAnswer += p.get(j);
                            }
                        }
                        avgEnt += entropy(p)*probAnswer;
                }
                if(accuracy < 0 || avgEnt < accuracy){
                    qIndex = currentQ;
                    accuracy = avgEnt;
                    text = currentQTxt;
                }
            }
            indexBestQ = qIndex;
            usedQs.add(text);
            questionsIndex.remove(Integer.valueOf(qIndex));
            return text;
        }

        public static void update(String ans){
        scores = updateScores(scores,ans,indexBestQ);
        }

        public static String getBestChar(){
        String charName = "";
        int score = 0;
            for(int i = 0; i < n; i++){
                if(scores.get(i) > score){
                    charName = names.get(i);
                    score = scores.get(i);
                }
            }
            return charName;
        }





}


