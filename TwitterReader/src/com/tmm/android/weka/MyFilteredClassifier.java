package com.tmm.android.weka;

/**

 * A Java class that implements a simple text classifier, based on WEKA.
 * To be used with MyFilteredLearner.java.
 * WEKA is available at: http://www.cs.waikato.ac.nz/ml/weka/
 * Copyright (C) 2013 Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 *
 * This program is free software: you can redistribute it and/or modify
 * it for any purpose.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import twitter4j.TwitterException;
import weka.core.*;
import weka.classifiers.meta.FilteredClassifier;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.tmm.android.twitter.TweetsActivity;
import com.tmm.android.twitter.util.GMailSender;

/**
 * This class implements a simple text classifier in Java using WEKA.
 * It loads a file with the text to classify, and the model that has been
 * learnt with MyFilteredLearner.java.
 * @author Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 * @see MyFilteredLearner
 */
public class MyFilteredClassifier {

	/**
	 * String that stores the text to classify
	 */
	String text;
	/**
	 * Object that stores the instance.
	 */
	Instances instances;
	/**
	 * Object that stores the classifier.
	 */
	FilteredClassifier classifier;

	/**
	 * This method loads the text to be classified.
	 * @param fileName The name of the file that stores the text.
	 */
	public void load(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			text = "";
			while ((line = reader.readLine()) != null) {
				text = text + " " + line;
			}
			System.out.println("===== Loaded text data: " + fileName + " =====");
			reader.close();
			System.out.println(text);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Problem found when reading: " + fileName);
		}
	}

	/**
	 * This method loads the model to be used as classifier.
	 * @param fileName The name of the file that stores the text.
	 */
	public void loadModel(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
			in.close();
			System.out.println("===== Loaded model: " + fileName + " =====");
		} 
		catch (Exception e) {
			// Given the cast, a ClassNotFoundException must be caught along with the IOException
			e.printStackTrace();
			System.out.println("Problem found when reading: " + fileName);
		}
	}

	/**
	 * This method creates the instance to be classified, from the text that has been read.
	 */
	public void makeInstance() {
		// Create the attributes, class and text
		FastVector fvNominalVal = new FastVector(2);
		fvNominalVal.addElement("happy");
		fvNominalVal.addElement("sad");
		//fvNominalVal.addElement("sad");
		fvNominalVal.addElement("fear");
		Attribute attribute1 = new Attribute("class", fvNominalVal);
		Attribute attribute2 = new Attribute("text",(FastVector) null);
		// Create list of instances with one element
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		instances = new Instances("Test relation", fvWekaAttributes, 1);           
		// Set class index
		instances.setClassIndex(0);
		// Create and add the instance
		DenseInstance instance = new DenseInstance(2);
		instance.setValue(attribute2, text);
		// Another way to do it:
		// instance.setValue((Attribute)fvWekaAttributes.elementAt(1), text);
		instances.add(instance);
		System.out.println("===== Instance created with reference dataset =====");
		System.out.println(instances);
	}

	/**
	 * This method performs the classification of the instance.
	 * Output is done at the command-line.
	 * @param ScreenName 
	 * @param mailID 
	 */
	public String classify(String ScreenName, String mailID) {
		try {
			double pred = classifier.classifyInstance(instances.instance(0));
			System.out.println("===== Classified instance =====");
			System.out.println("Class predicted: " + instances.classAttribute().value((int) pred));
			double[] probabilityDistribution = null;
			for (int i = 0; i < instances.numInstances(); i++)
			{
				probabilityDistribution = classifier.distributionForInstance(instances.instance(i));
			}
			double classAtt1Prob = probabilityDistribution[0];
			double classAtt2Prob = probabilityDistribution[1];
			//double classAtt3Prob = probabilityDistribution[2];
			System.out.println("Accuracy percentage" + classAtt1Prob*100);
			System.out.println("Accuracy percentage" + classAtt2Prob*100);
			//System.out.println("Accuracy percentage" + classAtt3Prob*100);
			if(instances.classAttribute().value((int) pred).equals("happy")){
				int y =(int) (100-(round(classAtt1Prob,2)*100));
				
				if(y>80){
					try {
						SendEmail(ScreenName,mailID,"sushil7271@gmail.com");
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return "sad"+" :- "+y+"%";
			}else{
				if(round(classAtt1Prob,2)*100>80){
					try {
						SendEmail(ScreenName,"sushil7271@gmail.com","Pallavi.phalke15@gmail.com");
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return instances.classAttribute().value((int) pred)+" :- "+round(classAtt1Prob,2)*100+"%";	
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem found when classifying the text");
			return null;

		}

	}
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	
	public void SendEmail(String username,String to,String from){
		String Subject="Emotional Support to your friend.";
		Spanned marked_up = Html.fromHtml("Hello Dear,<br><p>This is auto generated mail from "+ username+" regarding his/her current emotional state."
				+"Your friend’s Emotional Level is beyond 80% ,so he /she need support to prevent any wrong step.</p>"
				+"<b>HOW CAN YOU HELP YOUR FRIEND?</b><br>"
				+"<br>When do people need emotional support?"
				+"<br><p>People become upset for any number of different reasons. Distress can be a reaction to a common but disturbing life experience – an accident, a child hurt in a playground, someone injured in traffic – or after receiving bad news. Or it could be as a result of a very exceptional event, a plane crash, train derailment, major weather event or act of violence. Or it could be a build-up of many events, causing overload and stress."
				+"Whatever the cause of the emotional upset, the principles of helping are broadly the same. And they hold good whether you are helping a stranger in a first-aid situation, or a friend, colleague or relative.</p>"
				+"<b>What is the first step?</b>"
				+"<p>Carry out a quick but thoughtful assessment of the situation. What is happening? Are there any hazards? Notice who else is around. Are they likely to be helpful, or otherwise?"
				+"Then, crucially, check yourself. Think about what shape you are in. How have you been affected by the situation? The aim is to be calm. If you are calm, you can help others. If you aren\'t, you probably can\'t, at the moment."
				+"If you are calm enough to help someone else, that\'s good. If you are not, you might look for help for yourself.</p>"
				+"<b>How do you help someone who is upset?</b>"
				+"<p>Good listening is a very good start. It is harder, and rarer, than a lot of people think. Give people time to talk. Give them space, too – don\'t crowd them. Make eye contact appropriately, but don\'t stare. Be physically still and relaxed, not agitated or using sudden body movements. When you talk, use a calm voice. Don’t shout and don’t whisper. Don\'t interrupt."
				+"It is best to avoid false reassurance, such as, \"everything will be okay\". After all, it might not be. And even if it is, that is not how the person is feeling at that moment."
				+"Offer non-verbal encouragement—\"mmm\" and so on. That can indicate that you are listening, and are happy to hear what the person has to say. A good way to show you have understood is to to reflect out loud on what the person has said: “so, you’re very worried about that,” for instance."
				+"All the time, watch how the person is responding. Listen and learn from what they tell you about how they are feeling. Adapt your style to suit them."
				+"Accept their response – don’t argue or disagree with them. If you think something else is advisable, such as a medical check-up, calmly explain why.</p>"

				+"<b>What are things to avoid?</b>"
				+"Here are some basic mistakes to steer clear of:<br>"
				+"<br>- Don\'t try to jolly people up and get them to look at the funny side. They might do that later, but your task is to respect how they\'re feeling now and help them deal with it, not suppress it."
				+"<br>- Don\'t say things like, \"I know just how you are feeling, just the same happened to me\". This isn\'t empathy, it is more like boasting. It is very alienating and irritating."
				+"<br>- Don\'t hurry the next action. Always remember that a person who is upset is vulnerable and probably not in a state for successful decision-making.");

		try {   
			GMailSender sender = new GMailSender("Pallavi.phalke15@gmail.com", "Ganesha * 15");
			sender.sendMail(Subject, marked_up.toString(),  to, from);   
		} catch (Exception e) {   
			Log.e("SendMail", e.getMessage(), e);   
		} 
	}

	/**
	 * Main method. It is an example of the usage of this class.
	 * @param args Command-line arguments: fileData and fileModel.
	 *//*
	public static void main (String[] args) {

		MyFilteredClassifier classifier;
		if (args.length < 2)
			System.out.println("Usage: java MyClassifier <fileData> <fileModel>");
		else {
			classifier = new MyFilteredClassifier();
			classifier.load(args[0]);
			classifier.loadModel(args[1]);
			classifier.makeInstance();
			classifier.classify();
		}
	}*/
}	