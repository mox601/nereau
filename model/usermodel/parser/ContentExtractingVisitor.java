package model.usermodel.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.Translate;
import org.htmlparser.visitors.TextExtractingVisitor;

public class ContentExtractingVisitor extends TextExtractingVisitor {
	
    private StringBuffer textAccumulator;
    private boolean preTagBeingProcessed;
    private Tag lastTag;
    private final static String[] BAD_TAGS = {"SCRIPT","STYLE"};

    public ContentExtractingVisitor() {
        textAccumulator = new StringBuffer();
        preTagBeingProcessed = false;
        lastTag = null;
    }

    public String getExtractedText() {
        String result =  textAccumulator.toString();
        String commentPattern = "<!--.*?(-->)";
        Pattern pattern = Pattern.compile(commentPattern,Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        result = matcher.replaceAll(" ");
        
        result = result.replaceAll("[^0-9a-zA-z]{1,}", " ");
        result = result.toLowerCase().trim();
        return result;
    }

    public void visitStringNode(Text stringNode) {
        boolean flag = true;
        String lastTagName = lastTag==null ? "" : lastTag.getTagName();
        for(String tag: ContentExtractingVisitor.BAD_TAGS) {
        	if(tag.equals(lastTagName)) {
        		flag = false;
        		break;
        	}
        }
        if(flag)
    		this.expandAccumulator(stringNode);
    }
    
    private void expandAccumulator(Text stringNode) {
        String text = stringNode.getText();
        if (!preTagBeingProcessed) {
            text = Translate.decode(text);
            text = replaceNonBreakingSpaceWithOrdinarySpace(text) + " ";
        }
        textAccumulator.append(text); 	
    }

    private String replaceNonBreakingSpaceWithOrdinarySpace(String text) {
        return text.replace('\u00a0',' ');
    }

    public void visitTag(Tag tag)
    {
        if (isPreTag(tag)) {
            preTagBeingProcessed = true;
            
        }
        lastTag = tag;

    }

    public void visitEndTag(Tag tag)
    {
        if (isPreTag(tag))
            preTagBeingProcessed = false;
    }

    private boolean isPreTag(Tag tag) {
        return tag.getTagName().equals("PRE");
    }

}
