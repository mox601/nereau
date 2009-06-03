package test;

public abstract class TestParams {
	
	
	// root dei test: /Users/mox/Dati/Progetti/testingNereau
	
	
	public final static String test_specs = 		"/home/olbion/delicious2";
	public final static String dir = 				"/Users/mox/Dati/Progetti/testingNereau/nereau_corpus";
	public final static String data_dir = 			"/Users/mox/Dati/Progetti/testingNereau/nereau_corpus/data";
	public final static String index_dir = 			"/Users/mox/Dati/Progetti/testingNereau/nereau_corpus/index";
	public final static String noise_dir = 			"//Users/mox/Dati/Progetti/testingNereau/nereau_corpus/noise";
	public final static String training_dir = 		"training";
	public final static String test_dir = 			"test";
	public final static String delicious_prefix = 	"http://delicious.com/search?p=";
	public final static String delicious_suffix = 	"&context=all&lc=1&page=";
	//old works. useless: 
//	public final static String delicious_suffix_new = "&u=&chk=&context=main&fr=del_icio_us&lc=0";
	//int
	public final static int docs_per_tag_group = 100;
	public final static int training_docs_per_tag_group = 50;
	public final static int test_docs_per_tag_group = 50;
	public final static int docs_list = 150;
	public final static int docs_per_noise_group = 200;
	public final static int noise_list = 300;
	public final static int max_filename_length = 250;
	public final static int docs_per_evaluation = 10;

}
