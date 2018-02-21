// Thanapon Jarukasetphon 5888057 Sec1

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;

public class SimpleMovieRecommender implements BaseMovieRecommender{

	// HashMap
	private Map<Integer,Movie> movies;
	private Map<Integer,User> users;

	// HashBiMap For Predict
	private BiMap<Integer,Integer> loadModelMovies =  HashBiMap.create();
	private BiMap<Integer,Integer> loadModelUsers =  HashBiMap.create();
	private int model_NumUser;
	private int model_NumMovies;
	private double[][] matrix_rating;
	private double[][] matrix_similarity;

	/**
	 * Parse movieFilename and stores the movie information in a Map data structure
	 * that maps mid to the corresponding Movie object. If the input is null, simply
	 * return null.
	 * Each line in the file "movieFilename" has one of the following formats:
	 * 
	 * 1. The movie title does not contain ','
	 * 
	 * <mid>,<title> (<year>),<tag_1>|<tag_2>|<tag_3>|...|<tag_n>
	 * For example: 2,Jumanji (1995),Adventure|Children|Fantasy
	 * 
	 * 2. The movie title contains ','
	 * <mid>,"<title> (<year>)",<tag_1>|<tag_2>|<tag_3>|...|<tag_n>
	 * For example: 11,"American President, The (1995)",Comedy|Drama|Romance
	 * 
	 * The information of each movie must be store in a Movie object.
	 * 
	 * Return a Map<Integer, Movie> object that is a mapping from movie_id -> the corresponding Movie object.
	 * 
	 * Hint: You may find it easier to parse each line using regular expressions,
	 * and each tag set using String.split()
	 * 
	 * Note: the first line of the movie file contains the header information. Ignore it.
	 * 
	 */
	@Override
	public Map<Integer,Movie> loadMovies(String movieFilename){	
		
		// COMPLETE	
		Map<Integer,Movie> movies = new HashMap<Integer,Movie>();
		int mid;  // <-- movie id
		int year; // <-- year of movie
		String title; // <-- Title of movie
		String[] tmp = null; // Temporary String array that contains tags
	
		File file = new File(movieFilename);
		String ChampyString = null;
		try {
			ChampyString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error! movieFile Na Ja");	
		}

		//System.out.println("Read in: " + ChampyString);
		
		String[] ChampyStringNaJa = ChampyString.split("\n");
		
		//for(int i=0; i<ChampyStringNaJa.length;i++)
		//System.out.println(ChampyStringNaJa[i]);
		
		String pStr = "([0-9]+),[\"]*(.+) \\(([0-9]+)\\)[\"]*,(.+)";	//<--regular expression
		Pattern p = Pattern.compile(pStr);
		Matcher m;
		
		for(int a=0; a<ChampyStringNaJa.length; a++){
			
			m = p.matcher(ChampyStringNaJa[a]);
			//System.out.println(m.find());
			
			// *** Title of Movie must not only contain spaces (" "). ***
			if(m.find()&& !m.group(2).matches(" *")){	
				mid = Integer.parseInt(m.group(1));
					//System.out.print(mid +" ");
				title	= m.group(2);
					//System.out.print(title+" ");
				year = Integer.parseInt(m.group(3));
					//System.out.print(year+" ");			
				tmp = m.group(4).split("\\|"); // <--split tags of Movie to tmp array
				
				/*
				// check tags in tmp[]
				for(int i=0; i<tmp.length; i++){
						System.out.print(tmp[i]);
				}
				System.out.println();
				*/
				
				// Put movie id && movie into the map.
				Movie movieNaJa = new Movie(mid,title,year);				
				movies.put(movieNaJa.mid, movieNaJa);
				
				// Add tags to movie --> set of tags
				for(int i=0; i<tmp.length; i++){
					movies.get(mid).tags.add(tmp[i]);
				}
			}
		
		}
		return movies;
	}
	
	/**
	 * Parse the file "userFilename" which contains the information about movie ratings. 
	 * Each line in the file contains information about which user rates what movie, and when.
	 * 
	 * Each line in the file has the following format:
	 * 
	 * <uid>,<mid>,<rating>,<timestamp>
	 * For example: 1,256,0.5,1217895764
	 * 
	 * The information in each line must be stored in a Rating object.
	 * 
	 * Additionally, ratings associated with a user should be stored in the "ratings" object in the corresponding User object.
	 * 
	 * Return a Map<Integer, User> object that is a mapping from uid -> the corresponding User object.
	 * 
	 * If userFilename is null, just return null;
	 * 
	 * 
	 * Hint: In this case, String.split() may be less messier to use.
	 * 
	 * Note: the first line of the movie file contains the header information. Ignore it.
	 * 
	 * You may assume that this method is always called after loadMovies()
	 * 
	 */
	@Override
	public Map<Integer,User> loadUsers(String ratingFilename){
		
		// COMPLETE
		Map<Integer,User> users = new HashMap<Integer,User>();
		int uid;
		int mid;
		double rating;
		long timestamp;
	
		File file = new File(ratingFilename);
		String ratingString = null;
		try {
			ratingString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error! ratingFile Na Ja");	
		}
		//	System.out.println("Read in: " + ratingString);
		
		String pStr = "([0-9]+),([0-9]+),(.+),(.+)";	//<--regular expression	
		Pattern p = Pattern.compile(pStr);
		Matcher m = p.matcher(ratingString);
		User user;
		
		//System.out.println(ratingString);
		while(m.find()){		
			uid = Integer.parseInt(m.group(1)); // <--USER_ID
			//System.out.print(uid + " ");
			mid	= Integer.parseInt(m.group(2)); // <--MOVIE_ID
			//System.out.print(mid + " ");
			// rating = Double.parseDouble(m.group(3)); //<-- too slow / Doubles.tryParse() is faster
			rating = Doubles.tryParse(m.group(3)); // <--RATING
			//System.out.print(rating + " ");
			timestamp = Long.parseLong(m.group(4)); // <-- TIMESTAMP
			//System.out.print(timestamp+ "\n");
				
			// put user into the users map
			if(!users.containsKey(uid)){
				user = new User(uid);
				users.put(uid,user);
			}
			
			//System.out.println(movies.keySet());
			//System.out.println(mid + " > " + movies.get(mid));
			
			// add ratings --> set of rating
			users.get(uid).addRating(this.movies.get(mid),rating,timestamp);		
		}
		
		return users;
	}
	
	/**
	 * loadData() is called right after the recommender is instantiated. It loads the movies from "movieFilename"
	 * and users and ratings information from "ratingFilename", and store these pieces of information in the
	 * recommeder's internal memory. 
	 * 
	 * Specifically, this method makes calls to loadMovies(String movieFilename) and loadUsers(String ratingFilename) 
	 * respectively to parse and extract information from those input files.
	 * 
	 * @param movieFilename
	 * @param ratingFilename
	 */
	@Override
	public  void loadData(String movieFilename, String userFilename){
		// COMPLETE
		this.movies = loadMovies(movieFilename);
		this.users = loadUsers(userFilename);
	}
	
	/**
	 * Return the reference to the Map<Integer, Movie> object, which stores the loaded movies.
	 * 
	 * If the movies have not been loaded, there is 0 movie in the data, return an empty Map.
	 * 
	 */
	@Override
	public Map<Integer, Movie> getAllMovies(){
		// COMPLETE
		if(movies.isEmpty()) return null;
		return this.movies;
	}
	
	/**
	 * Return the reference to the Map<Integer, User> object, which stores the loaded users and their ratings.
	 * If the users/ratings have not been loaded, there is 0 rating in the data, return an empty Map.
	 * 
	 */
	@Override
	public Map<Integer, User> getAllUsers(){
		// COMPLETE
		if(users.isEmpty())	return null;
		return this.users;
	}
	
	/**
	 * Compute the user similarity between each pair of users, and produces an output model file "modelFilename"
	 * With the following format:
	 * 
	 * @NUM_USERS <num_users>
	 * @USER_MAP {0=<user_id1>, 1=<user_id2>, 2=<user_id2>, ...}
	 * @NUM_MOVIES <num_movies>
	 * @MOVIE_MAP {0=<movie_id1>, 2=<movie_id2>, ...}	
	 * @RATING_MATRIX	//is a num_users X (num_movies+1) matrix. Each element RATING_MATRIX(i,j) is the rating
	 * //the user index i gives to the movie index j. For example (from test case "micro")
	 * 4.0 0.0 1.5 4.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 3.1666666666666665 
	 * 0.0 3.5 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 3.5 
	 * 0.0 0.0 0.0 0.0 2.5 4.0 3.5 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 3.3333333333333335 
	 * @USERSIM_MATRIX	//is a num_users X num_users matrix. Each element USERSIM_MATRIX(i,j) is the similarity
	 * //score between the user index i and the user index j. For example (from test case "micro")
	 * 1.0000000000000002 0.0 0.0 
	 * 0.0 0.0 0.0 
	 * 0.0 0.0 1.0000000000000002 
	 * 
	 * You may assume that loadData() is called prior to the invocation of this method.
	 */
	@Override
	public  void trainModel(String modelFilename){
		
		// COMPLETE
		Map<Integer,Movie> removies = new TreeMap<Integer,Movie>(movies);
		Map<Integer,User> reusers = new TreeMap<Integer,User>(users);
		
		int i=0, j=0;
		double sumRating = 0;
		// Build StringBuilder for append value
		StringBuilder model = new StringBuilder(); 
		
		model.append("@NUM_USERS " + reusers.size() + "\n");
		model.append("@USER_MAP" + " {");
		for(User user:reusers.values()){
			model.append(i + "=" + user.uid);
			if(i!=reusers.size()-1){
				i++;
				model.append(", ");
			}
		}
		model.append("}" + "\n");
		model.append("@NUM_MOVIES " + removies.size() + "\n");
		model.append("@MOVIE_MAP" + " {");
		for(Movie movie: removies.values()){ 
			model.append(j + "=" + movie.mid);
			if(j!=removies.size()-1){
				j++;
				model.append(", ");
			}
		}
		model.append("}" + "\n");
		model.append("@RATING_MATRIX");
		
		// Rating Matrix
		for(User a:reusers.values()){
			sumRating = 0;
			model.append("\n");
			for(Movie b:removies.values()){
				if(a.ratings.containsKey(b.mid)){
					model.append(a.ratings.get(b.mid).rating);
					sumRating += a.ratings.get(b.mid).rating;
				}
				else{
					model.append("0.0");
				}
				model.append(" ");
			}
			model.append(sumRating/a.ratings.size());
		}
		model.append("\n");
		model.append("@USERSIM_MATRIX");
		
		// Similarity Matrix
		for(User u : reusers.values()){
			model.append("\n");
			for(User v : reusers.values()){
				model.append(similarity(u, v)+" ");
			}
		}
		
		// For check model
		//System.out.println(model);
		File file = new File(modelFilename);
		// convert StringBuilder to String then write it to file
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(model.toString());
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Loads the model file generated by trainModel() into the recommender.
	 * @param modelFilename
	 */
	@Override
	public  void loadModel(String modelFilename){
		
		// COMPLETE
		File file = new File(modelFilename);
		String ChampyString = null;
		try {
			ChampyString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error! modelFile Na Ja");	
		}
		//System.out.println("Read in: " + ChampyString);
				
		String[] array;
		// separate model(String ChampySting) into 6 group (not include array[0])
		array = ChampyString.split("@[\\nNUM_SERAPOVITGX{ ]+");
		
		/*//For check split
		for(int i=0; i<array.length;i++){
			System.out.print(array[i]);
		}
		*/
		
		//System.out.println();
		// @NUM_USERS
		this.model_NumUser = Integer.parseInt(array[1].replace("\n", ""));
		//System.out.println(this.model_NumUser);
		
		//************************** PARSE VALUE **************************//
		
		// @USER_MAP
		array[2] = array[2].replaceAll("}", "");
		String patternArray2 = "([0-9]+)=([0-9]+)";	//<--regular expression
		Pattern q = Pattern.compile(patternArray2);
		Matcher n = q.matcher(array[2]);
		int sequence;
		int id;
		while(n.find()){
			sequence = Integer.parseInt(n.group(1));
			id = Integer.parseInt(n.group(2));
			loadModelUsers.put(sequence, id);
		}
		//System.out.println(loadModelUsers); // checking HashBiMap
		
		// @NUM_MOVIES
		this.model_NumMovies = Integer.parseInt(array[3].replace("\n", ""));
		//System.out.println(this.model_NumMovies);
		
		// @MOVIE_MAP
		n = q.matcher(array[4]);
		while(n.find()){
			sequence = Integer.parseInt(n.group(1));
			id = Integer.parseInt(n.group(2));
			loadModelMovies.put(sequence, id);
		}
		//System.out.println(loadModelMovies);

		// @RATING_MATRIX
		array[5] = array[5].replace("\n", " ");
		// System.out.println(array[5]);
		matrix_rating = new double[this.model_NumUser][this.model_NumMovies+1];		
		String[] a = array[5].split(" ");
		
		int i=0;
		int j=0;
		
		for(int x=0;x<a.length;x++){
			matrix_rating[i][j] = Doubles.tryParse(a[x]);
			//System.out.print(matrix_rating[i][j]+" ");
			j++;
			if(j==this.model_NumMovies+1){
				i++;
				j=0;
			}
		}
		
		// @USERSIM_MATRIX
		// System.out.println();
		array[6] = array[6].replace("\n", "");
		// System.out.println(array[6]);
		
		matrix_similarity= new double[this.model_NumUser][this.model_NumUser];		
		String[] b = array[6].split(" ");
		i=0;
		j=0;
		
		for(int x=0;x<b.length;x++){
			matrix_similarity[i][j] = Doubles.tryParse(b[x]);
			//System.out.print(matrix_similarity[i][j]+" ");
			j++;
			if(j==this.model_NumUser){
				i++;
				j=0;
			}
		}		
	}
	
	/**
	 * Predicts the rating that the user "u" would give to the movie "m".
	 * The range of predicted rating must be [0,5.0] inclusive.
	 * You may assume that loadData() and loadModel() is called prior to the invocation of
	 * this method.
	 * 
	 * If u does not exist in the training file, simple return u.getMeanRating();
	 * @param m
	 * @param u
	 * @return
	 */
	@Override
	public double predict(Movie m, User u){
		
		// COMPLETE
		// If User u is null // special case
		if(u==null){
			return 0.0;
		}
		
		// If u does not exist in the training file
		if(!loadModelUsers.containsValue(u.uid) || !loadModelMovies.containsValue(m.mid)){
			return u.getMeanRating();
		}
		
		int pos_mov = loadModelMovies.inverse().get(m.mid); // <-- Find position of Movie in HashBiMap (loadModelMovies).
		int pos_user = loadModelUsers.inverse().get(u.uid); // <-- Find position of User in HashBiMap (loadModelUsers).
		double numerator = 0;
		double denominator = 0;
		double rate;
		double predict;
		
		for(int i=0; i<model_NumUser; i++){
			if(i==pos_user){continue;}
			if(matrix_rating[i][pos_mov] != 0.0){
				denominator += Math.abs(matrix_similarity[pos_user][i]);
				rate = matrix_rating[i][pos_mov] - matrix_rating[i][model_NumMovies];
				numerator += matrix_similarity[pos_user][i] * rate;
			}
		}		
	
		if(denominator==0){
			return matrix_rating[pos_user][model_NumMovies];
		}
		
		predict = matrix_rating[pos_user][model_NumMovies] + (numerator/denominator);
		
		// The range of predicted rating must be [0,5.0] inclusive.
		if(predict<0){return 0.0;}
		if(predict>5){return 5.0;}
		
		return predict;
	}
	
	
	// ADDMORE METHOD
	/**
	 * This method compute similarity between User u and v.
	 * If u and v is the same user, it will return 1.0.
	 * Value that return from this method must not be NaN.
	 * The range of similarity must be [-1,1] inclusive.
	 * @param u
	 * @param v
	 * @return similarity(double)
	 */
	public double similarity(User u, User v){
		
		// COMPLETE
		if(u == v){		// <--u and v is same user
			return 1.0;
		}
		
		int i = 0;
		double[] rui = new double[this.movies.size()];
		double[] rvi = new double[this.movies.size()];
		double numerator = 0;
		double denominator1 = 0;
		double denominator2 = 0;
		// Called getMeanRating one time and put it into variable for reducing times
		double meanRatingOfu = u.getMeanRating();
		double meanRatingOfv = v.getMeanRating(); 
		
		// Intersect rating set of u and v --> follow the formula
		for(int key : Sets.intersection(u.ratings.keySet(), v.ratings.keySet())){
			rui[i] = u.ratings.get(key).rating - meanRatingOfu;
			rvi[i] = v.ratings.get(key).rating - meanRatingOfv;
			i++;			
		}
		
		for(int j=0; j<rui.length; j++){
			numerator += rui[j]*rvi[j];
			denominator1 += Math.pow(rui[j], 2);
			denominator2 += Math.pow(rvi[j], 2);
		}
		
		//  If denominator is 0 the result will be NaN(Not-a-Number).
		if(denominator1==0.0 || denominator2==0.0){
			return 0.0;
		}
		
		double similarity = numerator / Math.sqrt(denominator1 * denominator2);
		if(similarity<-1){return -1.0;}
		if(similarity>1){return 1.0;}
	
		return similarity;
	}
	
	/**
	 * Compute the predicted ratings for all the movies produced during "fromYear" and "toYear", with respect to the user "u".
	 * Return the top K movies ranked by the the predicted ratings, represented by a List<MovieItem> object. 
	 * Note that a MovieItem object is used to hold the Movie object and its predicted rating. MovieItem is "Comparable"
	 * so you can call Collections.sort() on the list of such objects to sort them based on the predict ratings.  
	 * If the number of movies is fewer than K, simply return the ranked list of the movies.
	 * @param user
	 * @param fromYear
	 * @param toYear
	 * @param K
	 * @return
	 */
	@Override
	public  List<MovieItem> recommend(User u, int fromYear, int toYear, int K){
		
		// COMPLETE	
		// Build ArrayList for contain MovieItem
		List<MovieItem> rankMovies = new ArrayList<MovieItem>();
		
		// Find the movie that produced during [fromYear,toYear].
		for(Movie a: this.movies.values()){
			if(a.year>=fromYear && a.year<=toYear){
				rankMovies.add(new MovieItem(a,predict(a, u)));
			}
		}
		
		// Sorting rankMovies
		Collections.sort(rankMovies);
	
		// If size of rankMovies is less than K then return the ranked list of the movies.
		if(rankMovies.size()<K){
			return rankMovies;
		}
	
		// top K movies
		return rankMovies.subList(0, K);
	}
	
}
