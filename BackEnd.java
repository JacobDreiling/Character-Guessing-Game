import java.util.*;
import java.io.*;

public class BackEnd{
    public static int n = 0;
    public static int[][] score_table = {
            {2,1,0,-2},
            {1,1,0,-1},
            {0,0,0,0},
            {-2,-1,0,2}
    };
    public static String[] validResponses = {"Yes", "No", "Maybe", "I don't know"};
    public static ArrayList<ArrayList<String>> answers = new ArrayList<ArrayList<String>>();
    public static ArrayList<String> usedQs = new ArrayList<String>();
    public static ArrayList<String> names = new ArrayList<String>();
    public static ArrayList<Integer> scores = new ArrayList<Integer>();
    public static ArrayList<Integer> questionsIndex = new ArrayList<Integer>();
    public static ArrayList<String> questions = new ArrayList<String>();
    public static int indexBestQ = -1;

    public static void main(String[] args) throws IOException{
        initialize();
        Scanner in = new Scanner(System.in);
        for(int i=0; i<20; i++){
            System.out.println(getNextQuestion());
            update(in.nextLine());
        }
        System.out.println("Is "+getBestChar()+" your character?");
        if(in.nextLine().equals("yes"))
            System.out.println("Yay I win!");
        else
            System.out.println("Dang you win this time");
    }
    
    public static void initialize() throws IOException{
        // initializing the game
        // I create an ArrayList for the questions
        List<List<String>> table = new ArrayList<>();
        // reading from the csv file
        BufferedReader br = new BufferedReader(new FileReader("src/CharacterTable.csv"));
        String line;
        // getting the values from the csv file and adding them to the table array
        while((line = br.readLine()) != null){
            String[] values = line.split(",", -1);
            table.add(Arrays.asList(values));
        }

        // creating an ArrayList for the questions and loading it with the values from the table
        for(int i = 1; i < table.get(0).size(); i++){
            questions.add(table.get(0).get(i));
            questionsIndex.add(i-1);
        }

        for(int i = 1; i < table.size(); i++){
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
                else
                    new_answers.add(value);
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
        for(int x = 0; x < P.size(); x++){
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

    public static int score(String s1, String s2){
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
                i1 = 3;
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
                i2 = 3;
        }
        return score_table[i1][i2];
    }

    public static ArrayList<Integer> updateScores(ArrayList<Integer> nums, String answer, int q){
        ArrayList<Integer> newNums = new ArrayList<Integer>();
        for(int i = 0; i < n; i++){
            String correctAns = answers.get(i).get(q);
            int val = score(answer,correctAns);
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
    		double avgEnt = 0.0;
    		for(String response: validResponses){
    			ArrayList<Double> P = probs(updateScores(scores,response,currentQ));
    			double probAnswer = 0.0;
    			for(int j = 0; j < n; j++){
    				if(response.equals(answers.get(j).get(currentQ)))
    					probAnswer += P.get(j);
    			}
    			avgEnt += entropy(P)*probAnswer;
    		}
    		if(accuracy < 0 || avgEnt < accuracy){
    			qIndex = currentQ;
    			text = questions.get(currentQ);
    			accuracy = avgEnt;
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
