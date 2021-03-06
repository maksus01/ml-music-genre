package ml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Test {

	public static void main(String[] args) 
	{
		String[] genres4 = {"blues", "classical", "country", "disco", "hiphop", "jazz", "metal", "pop", "reggae", "rock"};
		String[] genres = {"classical","jazz","metal","pop"};

		Integer[] ignore = new Integer[] {6,7,8,9,10,11,12,13,14,15,16,17};



		ArrayList<ArrayList<Vector>> data = new ArrayList<ArrayList<Vector>>();

		//Create an ArrayList of all the training data, seperated by genre. 
		try {
			for(String genre : genres)
			{
				data.add(Util.readData(genre, true));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Create the testing data. 
		ArrayList<Vector> testing = Util.testSet(data, new int[] {4,9,11,34,17,90,56,23});

		//All the training data in one list
		ArrayList<Vector> training = new ArrayList<Vector>();
		for(ArrayList<Vector> list : data)
		{
			training.addAll(list);
		}

		boolean crossVal = true;

		ArrayList<Integer> ign = new ArrayList<Integer>(Arrays.asList(ignore));

		ArrayList<Vector> reducedTraining = Util.reduceSpace(training, ign);
		ArrayList<Vector> reducedTesting = Util.reduceSpace(testing, ign);

		//ArrayList<Vector> allT = Util.applyProductTransform(all);
		//ArrayList<Vector> testingT = Util.applyProductTransform(testing);

		//runSVM(genres, crossVal, reducedTraining, reducedTesting);

		//miniTest();

		//runAdaBoost(genres, crossVal, training, testing);
		runSVM(genres, crossVal, training, testing);

	}


	private static void miniTest()
	{
		double[][] trainingdata = new double[][] {{1,4,6},{1,5,7},{1,5,8},{1,3,0},{1,2,2},{1,3,3},{1,7,5},{1,8,6},{1,9,5}};
		//double[][] testdata = new double[][] {{4},{6},{500},{501},{499},{3},{-200},{-234},{-213}};
		ArrayList<Vector> training = new ArrayList<Vector>();
		ArrayList<Vector> test = new ArrayList<Vector>();
		double[][] testingdata = new double[][] {{1,2,1},{1,3,2},{1,4,7},{1,3,7},{1,8,5},{1,10,8}};
		for(int i = 0; i < trainingdata.length; i++)
		{
			String label;
			if(i < 3)
				label = "A";
			else if (i < 6)
				label = "B";
			else
				label = "C";
			Vector put = new Vector(trainingdata[i],label);
			training.add(put);
		}
		for(int i = 0; i < testingdata.length; i++)
		{
			String label;
			if(i < 2)
				label = "B";
			else if (i < 4)
				label = "A";
			else
				label = "C";
			Vector put = new Vector(testingdata[i],label);
			test.add(put);
		}

		//HashMap<Vector, Integer> kmlabels = KMeans.trainClassifier(training, 3);


		//Iterator it = kmlabels.entrySet().iterator();
		//		while(it.hasNext())
		//		{
		//			Map.Entry<Vector, Integer> pair = (Entry<Vector, Integer>)it.next();
		//			Vector temp = pair.getKey();
		//			System.out.println(temp + "\t" + pair.getValue());
		//		}

		ArrayList<Vector> classifiers = new ArrayList<Vector>();
		ArrayList<Object[]> ada = new ArrayList<Object[]>();
		String[] labels = new String[] {"A","B","C"};
		for(String label : labels)
		{
			Object[] adaboost = AdaBoost.trainClassifier(training, label);
			ada.add(adaboost);
			//Vector weight = SVM.SGD(training, .01, 20, 1000, label);
			//classifiers.add(weight);
			//System.out.println(label + " " + Util.collectAccuracy(test, weight, label));
		}

		int counter = 0;
		for(Object[] a : ada)
		{
			System.out.println("AdaBoost: " + labels[counter++]);
			for(Vector example : test)
			{
				System.out.println(example.getStringLabel() + " " + AdaBoost.predict(a, example));
			}
			//				String label = Util.predictLabel(ada, labels, example);
			//				System.out.println("Actual: " + example.getStringLabel() + "\tPredict: " + label);
			System.out.println("---------");
		}



		for(Vector v : classifiers)
			System.out.println(v);
	}


	private static void runSVM(String[] genres, boolean crossVal, ArrayList<Vector> training, ArrayList<Vector> testing)
	{
		if(crossVal)
		{
			double[] C = {0.0001,0.001, 0.01, 0.1, 1, 10,100,200};
			double[] Rho = {0.0001, 0.001, 0.01, 0.1, 1, 0.5, 0.002, 0.005};

			double[][] best = new double[genres.length][2];

			for(int i = 0; i < genres.length; i++)
			{
				best[i] = SVM.crossValidation(training, 10, false, genres[i], C, Rho);
			}

			for(int i = 0; i < genres.length; i++)
			{
				System.out.println(genres[i]+ ": " + best[i][0] + " " + best[i][1]);
			}
			ArrayList<Vector> classifiers = new ArrayList<Vector>();
			for(int i = 0; i < genres.length; i++)
			{
				//System.out.println(genres[i] + "classification");
				Vector weight = SVM.SGD(training, best[i][1], best[i][0], 100, genres[i]);
				classifiers.add(weight);
				System.out.println(genres[i] + " " + Util.collectAccuracy(testing, weight, genres[i]));
			}
			for(Vector example : testing)
			{
				String label = Util.predictLabel(classifiers, genres, example);
				System.out.println("Actual: " + example.getStringLabel() + "\tPredict: " + label);
			}
		}
		else
		{
			double c = 1.0;
			double r = 0.1;

			ArrayList<Vector> classifiers = new ArrayList<Vector>();

			for(String genre : genres)
			{
				Vector weight = SVM.SGD(training, r, c, 20, genre);
				classifiers.add(weight);
				System.out.println(genre + " " + Util.collectAccuracy(testing, weight, genre));
			}


			for(Vector example : testing)
			{
				String label = Util.predictLabel(classifiers, genres, example);
				System.out.println("Actual: " + example.getStringLabel() + "\tPredict: " + label);
			}
		}

	}

	private static void runLogisticRegression(String[] genres, boolean crossVal, ArrayList<Vector> training, ArrayList<Vector> testing)
	{
		if(crossVal)
		{
			double[] Sigma = {10, 20, 50, 100, 200, 400};
			double[] Rho = {0.0001, 0.001, 0.01, 0.1, 1};

			double[][] best = new double[genres.length][2];

			for(int i = 0; i < genres.length; i++)
			{
				best[i] = LogisticRegression.crossValidation(training, 10, false, genres[i], Sigma, Rho);
			}

			for(int i = 0; i < genres.length; i++)
			{
				System.out.println(genres[i]+ ": " + best[i][0] + " " + best[i][1]);
			}
			ArrayList<Vector> classifiers = new ArrayList<Vector>();
			for(int i = 0; i < genres.length; i++)
			{
				//System.out.println(genres[i] + "classification");
				Vector weight = LogisticRegression.SGD(training, best[i][1], best[i][0], 100, genres[i]);
				classifiers.add(weight);
				System.out.println(genres[i] + " " + Util.collectAccuracy(testing, weight, genres[i]));
			}
			for(Vector example : testing)
			{
				String label = Util.predictLabel(classifiers, genres, example);
				System.out.println("Actual: " + example.getStringLabel() + "\tPredict: " + label);
			}
		}
		else
		{
			double s = 1;
			double r = 0.1;

			ArrayList<Vector> classifiers = new ArrayList<Vector>();

			for(String genre : genres)
			{
				Vector weight = LogisticRegression.SGD(training, r, s, 20, genre);
				classifiers.add(weight);
				System.out.println(genre + " " + Util.collectAccuracy(testing, weight, genre));
			}


			for(Vector example : testing)
			{
				String label = Util.predictLabel(classifiers, genres, example);
				System.out.println("Actual: " + example.getStringLabel() + "\tPredict: " + label);
			}
		}
	}

	private static void runKMeans(String[] genres, boolean crossVal, ArrayList<Vector> training, ArrayList<Vector> testing)
	{
		HashMap<Vector, Integer> classifier = KMeans.trainClassifier(training, genres.length);
		ArrayList<ArrayList<Vector>> collect = new ArrayList<ArrayList<Vector>>(genres.length);
		Iterator it = classifier.entrySet().iterator();
		for(int i = 0; i < genres.length; i++)
			collect.add(new ArrayList<Vector>());
		while(it.hasNext())
		{
			Map.Entry<Vector, Integer> pair = (Entry<Vector, Integer>)it.next();
			Vector vec = pair.getKey();
			int loc = pair.getValue();			
			collect.get(loc).add(vec);			
		}

		for(int i = 0; i < collect.size(); i++)
		{
			System.out.println("====================== CLUSTER "+i+" =============================");
			for(int j = 0; j < collect.get(i).size(); j++)
			{
				System.out.println(collect.get(i).get(j).getStringLabel());
			}
		}
	}

	private static void runAdaBoost(String[] genres, boolean crossVal, ArrayList<Vector> training, ArrayList<Vector> testing)
	{
		ArrayList<Object[]> classifiers = new ArrayList<Object[]>();
		for(String label : genres)
		{
			classifiers.add(AdaBoost.trainClassifier(training, label));
		}


		for(Vector example : testing)
		{
			System.out.println(example.getStringLabel() + " was labeled as " + AdaBoost.predictLabel(classifiers, genres, example));

		}

	}

}
