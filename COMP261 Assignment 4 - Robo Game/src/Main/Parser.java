package Main;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;


/** The parser and interpreter.
    The top level parse function, a main method for testing, and several
    utility methods are provided.
    You need to implement parseProgram and all the rest of the parser.
 */

public class Parser {

    static HashMap<String, Integer> variables = new HashMap<String, Integer>(); //Holds variables
    static boolean challengeVar = false; // whether to turn on variable declaration before use

    /**
     * Top level parse method, called by the World
     */
    static RobotProgramNode parseFile(File code){
	Scanner scan = null;
	try {
	    scan = new Scanner(code);

	    // the only time tokens can be next to each other is
	    // when one of them is one of (){},;
	    scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

	    RobotProgramNode n = parseProgram(scan);  // You need to implement this!!!

	    scan.close();
	    return n;
	} catch (FileNotFoundException e) {
	    System.out.println("Robot program source file not found");
	} catch (ParserFailureException e) {
	    System.out.println("Parser error:");
	    System.out.println(e.getMessage());
	    scan.close();
	}
	return null;
    }

    /** For testing the parser without requiring the world */

    public static void main(String[] args){
	if (args.length>0){
	    for (String arg : args){
		File f = new File(arg);
		if (f.exists()){
		    System.out.println("Parsing '"+ f+"'");
		    RobotProgramNode prog = parseFile(f);
		    System.out.println("Parsing completed ");
		    if (prog!=null){
			System.out.println("================\nProgram:");
			System.out.println(prog);}
		    System.out.println("=================");
		}
		else {System.out.println("Can't find file '"+f+"'");}
	    }
	} else {
	    while (true){
		JFileChooser chooser = new JFileChooser(".");//System.getProperty("user.dir"));
		int res = chooser.showOpenDialog(null);
		if(res != JFileChooser.APPROVE_OPTION){ break;}
		RobotProgramNode prog = parseFile(chooser.getSelectedFile());
		System.out.println("Parsing completed");
		if (prog!=null){
		    System.out.println("Program: \n"+prog);
		}
		System.out.println("=================");
	    }
	}
	System.out.println("Done");
    }

    // Useful Patterns
    private static Pattern NUMPAT = Pattern.compile("-?\\d+");  //("-?(0|[1-9][0-9]*)");
    private static Pattern OPENPAREN = Pattern.compile("\\(");
    private static Pattern CLOSEPAREN = Pattern.compile("\\)");
    private static Pattern OPENBRACE = Pattern.compile("\\{");
    private static Pattern CLOSEBRACE = Pattern.compile("\\}");


    /**    PROG  ::= STMT+
     */
    static RobotProgramNode parseProgram(Scanner s){

	//If there is nothing to parse, throw fail exception.
	if (!s.hasNext()){
	    fail("Nothing to parse", s); //throw an exception because there is nothing to parse.
	    //return null;// just so it will compile!!
	}

	ProgramNode node = new ProgramNode();

	while (s.hasNext()){
	    node.addNode(parseStmt(s));

	}
	return node;
    }

    static RobotProgramNode parseStmt(Scanner s) {
	//If it is any of the actions, call parse action.
	if (s.hasNext("move") || s.hasNext("turnL") || s.hasNext("turnR") || s.hasNext("takeFuel") || s.hasNext("wait") || s.hasNext("turnAround") || s.hasNext("shieldOn") || s.hasNext("shieldOff")){
	    StmtNode node = new StmtNode(parseAct(s));
	    if (s.hasNext(";")){
		s.next();
		return node;
	    }
	    fail("Missing ';' ", s);
	} 
	else if (s.hasNext("loop")) {
	    StmtNode node = new StmtNode(parseLoop(s));
	    return node;
	}
	else if (s.hasNext("if")){
	    StmtNode node = new StmtNode(parseIf(s));
	    return node;
	}
	else if (s.hasNext("while")){
	    StmtNode node = new StmtNode(parseWhile(s));
	    return node;
	}
	else if (s.hasNext("\\$[A-Za-z][A-Za-z0-9]*")){
	    StmtNode node = new StmtNode(parseAssgn(s));
	    return node;
	}
	fail("Invalid statement", s);
	return null;  // just so it will compile!!
    }

    static RobotProgramNode parseAssgn(Scanner s) {
	if (s.hasNext("\\$[A-Za-z][A-Za-z0-9]*")) {
	    String variableName = s.next();

	    if(s.hasNext("=")) {
		s.next();
		ExpressionNode expression = parseExp(s);

		if (s.hasNext(";")){
		    s.next(); 
		    return new AssgnNode(variableName, expression);
		}
		fail("';' not found after variable declaration", s);
	    }
	    fail("'=' not found after variable name", s);
	}
	fail("Invalid variable name", s);

	return null;
    }

    static RobotProgramNode parseAct(Scanner s){
	if (s.hasNext("move")) {
	    s.next();

	    //If move has argument, parses argument and returns a move node with the expression.
	    if (s.hasNext(OPENPAREN)) {
		s.next();
		RobotProgramNode node = new MoveNode(parseExp(s));

		if (s.hasNext(CLOSEPAREN)){
		    s.next();
		    return node;
		}
		fail("No close parenthesis found after expression", s);
	    }

	    return new MoveNode(); //If there wasnt an argument, returns a move node without an argument
	}
	if (s.hasNext("turnL")) {
	    s.next();
	    return new TurnLNode();
	}
	if (s.hasNext("turnR")) {
	    s.next();
	    return new TurnRNode();
	}
	if (s.hasNext("takeFuel")) {
	    s.next();
	    return new TakeFuelNode();
	}
	if (s.hasNext("wait")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();
		RobotProgramNode node = new WaitNode(parseExp(s));

		if (s.hasNext(CLOSEPAREN)){
		    s.next();
		    return node;
		}
		fail("No close parenthesis found after expression", s);
	    }

	    return new WaitNode();
	}
	if (s.hasNext("turnAround")) {
	    s.next();
	    return new TurnAroundNode();
	}
	if (s.hasNext("shieldOn")) {
	    s.next();
	    return new ShieldOnNode();
	}
	if (s.hasNext("shieldOff")) {
	    s.next();
	    return new ShieldOffNode();
	}
	fail("Invalid Action", s);
	return null;
    }

    static ExpressionNode parseExp(Scanner s) {
	if (s.hasNext("-?[1-9][0-9]*|0")) {
	    return new NumNode(s.nextInt());
	}
	else if (isSen(s)){
	    return parseSen(s);
	}
	else if (s.hasNext("add") || s.hasNext("sub") || s.hasNext("mul") || s.hasNext("div")) {
	    return parseOp(s);
	}
	//Else if next token is a variable name
	else if (s.hasNext("\\$[A-Za-z][A-Za-z0-9]*")) { 
	    String variableName = s.next();

	    //If the variable already exists, get its expression. Else, create a new variable with the expression 0
	    if (variables.containsKey(variableName)) {
		return new VariableNode(variableName);
	    }

	    if (challengeVar) {
		fail("Variables must be declared before they are used in the program", s);
	    } else {
		variables.put(variableName, 0);
		return new VariableNode(variableName);
	    }
	}

	fail("No valid expression found", s);
	return null; //to make it compile
    }

    static ExpressionNode parseOp(Scanner s){
	if (s.hasNext("add")){
	    s.next();
	    if (s.hasNext(OPENPAREN)){
		s.next();
		ExpressionNode expression1 = parseExp(s);
		if (s.hasNext(",")){
		    s.next();
		    ExpressionNode expression2 = parseExp(s);
		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new AddNode(expression1, expression2);
		    }
		    fail("Missing closing parenthesis after expression", s);
		}
		fail("Missing ','", s);
	    }
	    fail("Missing open parenthesis", s);
	}

	if (s.hasNext("sub")){
	    s.next();
	    if (s.hasNext(OPENPAREN)){
		s.next();
		ExpressionNode expression1 = parseExp(s);
		if (s.hasNext(",")){
		    s.next();
		    ExpressionNode expression2 = parseExp(s);
		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new SubNode(expression1, expression2);
		    }
		    fail("Missing closing parenthesis after expression", s);
		}
		fail("Missing ','", s);
	    }
	    fail("Missing open parenthesis", s);
	}

	if (s.hasNext("mul")){
	    s.next();
	    if (s.hasNext(OPENPAREN)){
		s.next();
		ExpressionNode expression1 = parseExp(s);
		if (s.hasNext(",")){
		    s.next();
		    ExpressionNode expression2 = parseExp(s);
		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new MulNode(expression1, expression2);
		    }
		    fail("Missing closing parenthesis after expression", s);
		}
		fail("Missing ','", s);
	    }
	    fail("Missing open parenthesis", s);
	}

	if (s.hasNext("div")){
	    s.next();
	    if (s.hasNext(OPENPAREN)){
		s.next();
		ExpressionNode expression1 = parseExp(s);
		if (s.hasNext(",")){
		    s.next();
		    ExpressionNode expression2 = parseExp(s);
		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new DivNode(expression1, expression2);
		    }
		    fail("Missing closing parenthesis after expression", s);
		}
		fail("Missing ','", s);
	    }
	    fail("Missing open parenthesis", s);
	}
	return null;
    }


    static RobotProgramNode parseLoop(Scanner s) {
	if (s.hasNext("loop")) {
	    s.next();
	    return new LoopNode(parseBlock(s));
	}
	fail("'loop' statement not found", s);
	return null;
    }

    static RobotProgramNode parseIf(Scanner s) {
	if (s.hasNext("if") || s.hasNext("elif")){
	    s.next(); //pointer moves past "if"

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		RobotProgramNode ifNode = new IfNode(parseCond(s)); // creates a new if node and passes it the conditional node.

		if (s.hasNext(CLOSEPAREN)) {
		    s.next();

		    IfNode castIfNode = (IfNode) ifNode; //Cast ifnode to an ifnode object
		    castIfNode.setBlock((BlockNode)parseBlock(s)); //sets the block node inside the if node

		    while (s.hasNext("elif")) {
			castIfNode.addElif((IfNode)parseIf(s));
		    }

		    if(s.hasNext("else")) {
			s.next();
			castIfNode.setElseBlock((BlockNode)parseBlock(s));
		    }
		    return ifNode;
		} else {
		    fail("No close parenthesis found after conditional", s);
		}
	    } else {
		fail ("No open parenthesis found before conditional", s);
	    }
	} else {
	    fail("'if' statement not found ", s);
	}
	return null; // to make it compile
    }


    static RobotProgramNode parseWhile(Scanner s){
	if (s.hasNext("while")) {
	    s.next();
	    if (s.hasNext(OPENPAREN)) {
		s.next();

		RobotProgramNode whileNode = new WhileNode(parseCond(s));

		if (s.hasNext(CLOSEPAREN)) { 
		    s.next();
		    WhileNode castWhileNode = (WhileNode)whileNode;
		    castWhileNode.setBlock((BlockNode)parseBlock(s));
		    return whileNode;
		}
		fail("No close parenthesis found after conditioanl", s);
	    }
	    fail("No open parenthesis found before conditional", s);
	}
	fail ("no 'while' found", s);

	return null; // to make it compile
    }


    static ConditionalNode parseCond(Scanner s) {
	if (s.hasNext("lt")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		//SensorNode sen = parseSen(s);
		ExpressionNode e1 = parseExp(s);

		if (s.hasNext(",")) {
		    s.next();

		    //NumNode num = parseNum(s);
		    ExpressionNode e2 = parseExp(s);

		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			ConditionalNode node = new LessThanNode(e1, e2);
			return node;
		    } else {
			fail ("')' not found", s);
		    }
		} else {
		    fail ("',' not found", s);
		}
	    }
	    else {
		fail("No '(' found", s);
	    }
	}

	if (s.hasNext("gt")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		//SensorNode sen = parseSen(s);
		ExpressionNode e1 = parseExp(s);

		if (s.hasNext(",")) {
		    s.next();

		    //NumNode num = parseNum(s);
		    ExpressionNode e2 = parseExp(s);

		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			ConditionalNode node = new GreaterThanNode(e1, e2);
			return node;
		    } else {
			fail ("')' not found", s);
		    }
		} else {
		    fail ("',' not found", s);
		}
	    }
	    else { 
		fail("'(' not found ", s); 
	    }
	}

	if (s.hasNext("eq")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		//SensorNode sen = parseSen(s);
		ExpressionNode e1 = parseExp(s);

		if (s.hasNext(",")) {
		    s.next();

		    //NumNode num = parseNum(s);
		    ExpressionNode e2 = parseExp(s);

		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			ConditionalNode node = new EqualToNode(e1, e2);
			return node;
		    } else {
			fail ("')' not found", s);
		    }
		} else {
		    fail ("',' not found", s);
		}
	    }
	    else { 
		fail("'(' not found ", s); 
	    }
	}

	if (s.hasNext("and")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		ConditionalNode n1 = new ConditionNode(parseCond(s)); 

		if (s.hasNext(",")){
		    s.next();
		    ConditionalNode n2 = new ConditionNode(parseCond(s));

		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new AndNode(n1, n2);
		    }
		    fail("close parenthesis not found", s);
		}
		fail("',' not found", s);
	    }
	    fail("'(' not found", s);
	}


	if (s.hasNext("or")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		ConditionalNode n1 = new ConditionNode(parseCond(s)); 

		if (s.hasNext(",")){
		    s.next();
		    ConditionalNode n2 = new ConditionNode(parseCond(s));


		    if (s.hasNext(CLOSEPAREN)) {
			s.next();
			return new OrNode(n1, n2);
		    }
		    fail("close parenthesis not found", s);
		}
		fail("',' not found", s);
	    }
	    fail("'(' not found", s);
	}

	if (s.hasNext("not")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)) {
		s.next();

		ConditionalNode n1 = new ConditionNode(parseCond(s)); 

		if (s.hasNext(CLOSEPAREN)) {
		    s.next();
		    return new NotNode(n1);
		}
		fail("close parenthesis not found", s);
	    }
	    fail("'(' not found", s);
	}



	fail ("Invalid condition expression", s);
	return null; // to make it compile
    }


    static SensorNode parseSen(Scanner s) {
	if (s.hasNext("fuelLeft")) {
	    s.next();
	    SensorNode node = new FuelLeftNode();
	    return node;
	}

	if (s.hasNext("oppLR")) {
	    s.next();
	    SensorNode node = new OppLRNode();
	    return node;
	}

	if (s.hasNext("oppFB")) {
	    s.next();
	    SensorNode node = new OppFBNode();
	    return node;
	}

	if (s.hasNext("numBarrels")) {
	    s.next();
	    SensorNode node = new NumBarrelsNode();
	    return node;
	}

	if (s.hasNext("barrelLR")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)){
		s.next();

		ExpressionNode expression = parseExp(s);
		if (s.hasNext(CLOSEPAREN)){
		    s.next();
		    return new BarrelLRNode(expression);
		}
		fail("Missing close parenthesis after optional argument", s);
	    }

	    SensorNode node = new BarrelLRNode();
	    return node;
	}

	if (s.hasNext("barrelFB")) {
	    s.next();

	    if (s.hasNext(OPENPAREN)){
		s.next();

		ExpressionNode expression = parseExp(s);
		if (s.hasNext(CLOSEPAREN)){
		    s.next();
		    return new BarrelFBNode(expression);
		}
		fail("Missing close parenthesis after optional argument", s);
	    }

	    SensorNode node = new BarrelFBNode();
	    return node;
	}
	if (s.hasNext("wallDist")) {
	    s.next();
	    SensorNode node = new WallDistNode();
	    return node;
	}

	fail("Invalid sensor argument", s);

	return null; // to make it compile
    }


    static NumNode parseNum(Scanner s) {
	if (s.hasNext("-?[1-9][0-9]*|0")) {
	    return new NumNode(Integer.parseInt(s.next()));
	}
	fail("Invalid number argument", s);
	return null; //to make it compile
    }



    static RobotProgramNode parseBlock(Scanner s) {
	if (!s.hasNext(OPENBRACE)) {
	    fail("No open brace found", s);
	    //return null;
	}
	s.next(); //Pass the open brace

	////NOTE: MAY WANT TO CHANGE WHILE LOOP CONDITIONAL!!
	BlockNode node = new BlockNode(); //Create new block node
	while (!s.hasNext(CLOSEBRACE) && isStmt(s)) {
	    node.addNode(parseStmt(s));
	}


	if (node.getSize() == 0) {
	    fail ("No 'block' found inside loop", s);
	}


	if (!s.hasNext(CLOSEBRACE)) {
	    fail("No close brace found", s);
	    //return null;
	}
	s.next();

	return node;

    }

    //Checks if next part is part of a statement and returns a boolean. currently not used.
    static boolean isStmt(Scanner s) {
	if (s.hasNext("move") || s.hasNext("turnL") || s.hasNext("turnR") || s.hasNext("takeFuel") || s.hasNext("wait") || s.hasNext("turnAround") || s.hasNext("shieldOn") || s.hasNext("shieldOff") || s.hasNext("loop") || s.hasNext("if") || s.hasNext("while") || s.hasNext("\\$[A-Za-z][A-Za-z0-9]*")) {
	    return true;
	}
	fail("invalid statement or no close brace found", s);
	return false;
    }

    static boolean isSen(Scanner s) {
	if (s.hasNext("fuelLeft") || s.hasNext("oppLR") || s.hasNext("oppFB") || s.hasNext("numBarrels") || s.hasNext("barrelLR") || s.hasNext("barrelFB") || s.hasNext("wallDist")){
	    return true;
	}
	return false;
    }



    //utility methods for the parser
    /**
     * Report a failure in the parser.
     */
    static void fail(String message, Scanner s){
	String msg = message + "\n   @ ...";
	for (int i=0; i<7 && s.hasNext(); i++){
	    msg += " " + s.next();
	}
	throw new ParserFailureException(msg+"...");
    }

    /**
       If the next token in the scanner matches the specified pattern,
       consume the token and return true. Otherwise return false without
       consuming anything.
       Useful for dealing with the syntactic elements of the language
       which do not have semantic content, and are there only to
       make the language parsable.
     */
    static boolean gobble(String p, Scanner s){
	if (s.hasNext(p)) { s.next(); return true;} 
	else { return false; } 
    }
    static boolean gobble(Pattern p, Scanner s){
	if (s.hasNext(p)) { s.next(); return true;} 
	else { return false; } 
    }


}

//************************************************ CLASSES ************************************************//


//*************************** PROGRAM NODE ***************************//
class ProgramNode implements RobotProgramNode {

    private ArrayList<RobotProgramNode> children; //statement nodes

    public ProgramNode(){
	this.children = new ArrayList<RobotProgramNode>();
    }

    public void execute (Robot robot) {
	for (int i = 0; i < this.children.size(); i++) {
	    this.children.get(i).execute(robot);
	}
    }

    public void addNode(RobotProgramNode node) {
	this.children.add(node);
    }

    public String toString() {
	String str = "";
	for (int i = 0; i < this.children.size(); i++){
	    str += this.children.get(i).toString() + "\n";
	}
	return str;
    }
}

//*************************** STMT NODES ***************************//

class StmtNode implements RobotProgramNode{

    RobotProgramNode child; //either act, loop, if, while node

    public StmtNode(RobotProgramNode node) {
	this.child = node;
    }

    public String toString() {
	return this.child.toString();
    }

    public void execute (Robot robot) {
	child.execute(robot);
    }
}

class LoopNode implements RobotProgramNode {

    private RobotProgramNode child; //child is the block node

    public LoopNode(RobotProgramNode blockNode){
	this.child = blockNode;
    }

    public void execute(Robot robot) {
	while(true) {
	    this.child.execute(robot);
	}
    }

    public String toString() {
	System.out.println("called");
	return "loop " + child.toString() + "end loop";
    }
}


class IfNode implements RobotProgramNode {

    private ConditionalNode condition;
    private BlockNode block;
    private ArrayList<IfNode> elifList;
    private BlockNode elseBlock;

    public IfNode (ConditionalNode condition) {
	this.condition = condition;
    }

    public void setBlock(BlockNode block){
	this.block = block;
    }

    public void setElseBlock(BlockNode elseBlock){
	this.elseBlock = elseBlock;
    }

    public void addElif(IfNode node) {
	if (elifList == null) {
	    this.elifList = new ArrayList<IfNode>();
	}

	this.elifList.add(node);
    }


    public void execute(Robot robot) {
	if (condition.evaluate(robot)) {
	    block.execute(robot);
	} else {
	    if (this.elifList != null) {
		for (IfNode n : this.elifList){
		    n.execute(robot);
		    return; //exit after completing a valid if conditional
		}
	    }
	    if (elseBlock != null) {
		elseBlock.execute(robot);
	    }
	}
    }

    public String toString(){
	String str = "";

	str += "if ("+ condition.toString() + ")" + this.block.toString() + "}";

	if (elifList != null) {
	    for (IfNode n : this.elifList){
		str += "el"+ n.toString();
	    }
	}


	if (elseBlock != null) {
	    str += "else " + this.elseBlock.toString() + "}";
	}

	return str;
    }
}

class WhileNode implements RobotProgramNode {
    private ConditionalNode condition;
    private BlockNode block;

    public WhileNode (ConditionalNode condition) {
	this.condition = condition;
    }

    public void setBlock(BlockNode block){
	this.block = block;
    }

    public void execute(Robot robot) {
	while (condition.evaluate(robot)) {
	    block.execute(robot);
	}
    }
    public String toString(){
	return "while ("+ condition.toString() + ")" + this.block.toString() + "end while";
    }
}


//*************************** ACTION NODES ***************************//
/*MOVE NODE*/
class MoveNode implements RobotProgramNode {
    private ExpressionNode expression;
    private int count;

    //Empty constructor
    public MoveNode() {

    } 

    public MoveNode(ExpressionNode  expression) {
	this.expression = expression;
    }

    public void execute(Robot robot) {
	if (expression == null) {
	    robot.move();
	} else {
	    this.count = expression.evaluate(robot);
	    for (int i = 0; i < count; i++){
		robot.move();
	    }
	}
    }
    public String toString() {
	if (expression == null) {
	    return "move";
	}
	return "move " + expression.toString() + " number of times";
    }
}

/*TURN LEFT NODE*/
class TurnLNode implements RobotProgramNode {

    //Empty Constructor
    public TurnLNode() {}

    public void execute(Robot robot) {
	robot.turnLeft();
    }

    public String toString() {
	return "turn left";
    }
}
/*TURN RIGHT NODE*/
class TurnRNode implements RobotProgramNode {

    //Empty constructor
    public TurnRNode() {}

    public void execute(Robot robot) {
	robot.turnRight();
    }

    public String toString() {
	return "turn right";
    }
}

/*TURN AROUND NODE*/
class TurnAroundNode implements RobotProgramNode{

    public void execute(Robot robot) {
	robot.turnAround();
    }

    public String toString() {
	return "Turn Around";
    }
}

/*SHIELD ON NODE*/
class ShieldOnNode implements RobotProgramNode {

    public void execute(Robot robot) {
	robot.setShield(true);
    }

    public String toString() {
	return "Shield On";
    }
}

/*SHIELD OFF NODE*/
class ShieldOffNode implements RobotProgramNode{

    public void execute(Robot robot) {
	robot.setShield(false);
    }

    public String toString() {
	return "Shield Off";
    }
}

/*TAKE FUEL NODE*/
class TakeFuelNode implements RobotProgramNode {
    //Empty constructor
    public TakeFuelNode() {}

    public void execute(Robot robot) {
	robot.takeFuel();
    }

    public String toString() {
	return "take fuel";
    }
}

/*WAIT NODE*/
class WaitNode implements RobotProgramNode {

    private ExpressionNode expression;
    private int count;

    public WaitNode(){}

    public WaitNode(ExpressionNode exp){
	this.expression = exp;
    }

    public void execute(Robot robot) {
	if (this.expression == null) {
	    robot.idleWait(); //Waits!
	}
	else {
	    this.count = this.expression.evaluate(robot);
	    for (int i =0; i < count; i++) {
		robot.idleWait();
	    }
	}
    }

    public String toString() {
	if (this.expression == null) {
	    return "wait";
	} else {
	    return "wait 'expression' number of times";
	}
    }
}


//*************************** COND NODES ***************************//
/*CONDITIONAL NODE*/
class ConditionNode implements ConditionalNode{
    ConditionalNode n;

    public ConditionNode(ConditionalNode n){
	this.n = n;
    }


    public boolean evaluate(Robot robot){
	return n.evaluate(robot);
    }

    public String toString() {
	return n.toString();
    }
}

/*AND NODE*/
class AndNode implements ConditionalNode{
    ConditionalNode n1;
    ConditionalNode n2;

    public AndNode(ConditionalNode n1, ConditionalNode n2) {
	this.n1 = n1;
	this.n2 = n2;
    }

    public boolean evaluate(Robot robot) {
	return (n1.evaluate(robot) && n2.evaluate(robot));
    }

    public String toString() {
	return n1.toString() + " AND " + n2.toString();
    }
}


/*NOT NODE*/
class NotNode implements ConditionalNode{
    ConditionalNode n1;

    public NotNode(ConditionalNode n1) {
	this.n1 = n1;
    }

    public boolean evaluate(Robot robot) {
	return (!n1.evaluate(robot));
    }


    public String toString() {
	return "NOT " + n1.toString();
    }
}

/*OR NODE*/
class OrNode implements ConditionalNode{
    ConditionalNode n1;
    ConditionalNode n2;

    public OrNode(ConditionalNode n1, ConditionalNode n2) {
	this.n1 = n1;
	this.n2 = n2;
    }

    public boolean evaluate(Robot robot) {
	return (n1.evaluate(robot) || n2.evaluate(robot));
    }

    public String toString() {
	return n1.toString() + " OR " + n2.toString();
    }
}


/*GREATER THAN NODE*/
class GreaterThanNode implements ConditionalNode {
    private ExpressionNode e1;
    private ExpressionNode e2;

    public GreaterThanNode(ExpressionNode e1, ExpressionNode e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

    public boolean evaluate(Robot robot) {
	if (e1.evaluate(robot) > e2.evaluate(robot)){
	    return true;
	}
	return false;
    }

    public String toString() {
	return "(" + e1.toString() + " > " + e2.toString() + ")";
    }
}

/*EQUAL TO NODE*/
class EqualToNode implements ConditionalNode {

    private ExpressionNode e1;
    private ExpressionNode e2;

    public EqualToNode(ExpressionNode e1, ExpressionNode e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

    public boolean evaluate(Robot robot) {
	if (e1.evaluate(robot) == e2.evaluate(robot)){
	    return true;
	}
	return false;
    }

    public String toString() {
	return "(" + e1.toString() + " == " + e2.toString() + ")";
    }
}

/*LESS THAN NODE*/
class LessThanNode implements ConditionalNode{

    private ExpressionNode e1;
    private ExpressionNode e2;

    public LessThanNode(ExpressionNode e1, ExpressionNode e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

    public boolean evaluate(Robot robot) {
	if (e1.evaluate(robot) < e2.evaluate(robot)){
	    return true;
	}
	return false;
    }

    public String toString() {
	return "(" + e1.toString() + " < " + e2.toString() + ")";
    }
}


//*************************** ASSGN NODE ***************************//
/*ASSGN NODE*/
class AssgnNode implements RobotProgramNode{

    private String name;
    private ExpressionNode expression;

    public AssgnNode(String name, ExpressionNode expression) {
	this.name = name;
	this.expression = expression;
    }

    public void setExpression(ExpressionNode expression){
	this.expression = expression;
    }

    public void setName(String name){
	this.name = name;
    }

    public void execute(Robot robot) {
	Parser.variables.put(this.name, this.expression.evaluate(robot));
    }

    public String toString() {
	return name.toString() + " = "+ expression.toString();
    }
}

//*************************** SENSOR NODES ***************************//
/*FUEL LEFT NODE*/
class FuelLeftNode implements SensorNode, ExpressionNode {
    public int execute(Robot robot) {
	return robot.getFuel();
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }

    public String toString() {
	return "Fuel remaining";
    }
}

/*OPP FB NODE*/
class OppFBNode implements SensorNode, ExpressionNode {
    public int execute(Robot robot) {
	return robot.getOpponentFB();
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }

    public String toString()  {
	return "Opponents FB Position";
    }
}

/*OPP LR NODE*/
class OppLRNode implements SensorNode, ExpressionNode {

    public int execute(Robot robot) {
	return robot.getOpponentLR();
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }

    public String toString() { 
	return "Opponents LR Position";

    }
}

/*BARREL FB NODE*/
class BarrelFBNode implements SensorNode, ExpressionNode {

    private ExpressionNode expression;

    public BarrelFBNode() {

    }

    public BarrelFBNode(ExpressionNode expression) {
	this.expression = expression;
    }

    public int execute(Robot robot) {
	return robot.getClosestBarrelFB(); // get the first barrel????
    }

    public int evaluate(Robot robot) {
	if (expression == null) {
	    return robot.getClosestBarrelFB(); // get the first barrel????
	} else {
	    return robot.getBarrelFB(this.expression.evaluate(robot));
	}
    }

    public String toString()  {
	if (expression == null) {
	    return "FB distance to closest barrel";
	}
	else {
	    return "FB distance to barrel no. 'argument'";
	}
    }
}

/*BARREL LR NODE*/
class BarrelLRNode implements SensorNode, ExpressionNode{

    private ExpressionNode expression;

    public BarrelLRNode() {

    }

    public BarrelLRNode(ExpressionNode expression) {
	this.expression = expression;
    }


    public int execute(Robot robot) {
	if (expression == null) {
	    return robot.getClosestBarrelLR(); // get the first barrel????
	} else {
	    return robot.getBarrelLR(this.expression.evaluate(robot));
	}
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }


    public String toString()  {
	if (this.expression == null) { 
	    return "LR distance to closest barrel";
	} else {
	    return "LR distance to barrel no. 'argument'";
	}
    }

}

/*NUM BARRELS NODE*/
class NumBarrelsNode implements SensorNode, ExpressionNode {
    public int execute(Robot robot) {
	return robot.numBarrels();
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }

    public String toString()  {
	return "No. of Barrels Currently in the word";
    }
}

/*WALL DISTANCE NODE*/
class WallDistNode implements SensorNode, ExpressionNode {
    public int execute(Robot robot) {
	return robot.getDistanceToWall();
    }

    public int evaluate(Robot robot) {
	return execute(robot);
    }

    public String toString()  {
	return "Distance to wall";
    }
}



//*************************** BLOCK NODE ***************************//
/*BLOCK NODE*/
class BlockNode implements RobotProgramNode {

    private ArrayList<RobotProgramNode> statements;

    public BlockNode() {
	this.statements = new ArrayList<RobotProgramNode>();
    }

    public void execute (Robot robot) {
	for (int i = 0; i < this.statements.size(); i++) {
	    this.statements.get(i).execute(robot);
	}
    }

    public void addNode(RobotProgramNode node) {
	this.statements.add(node);
    }

    public int getSize(){
	return this.statements.size();
    }

    public String toString() {
	String str = "\n";

	for (RobotProgramNode r : this.statements) {
	    str += "\t" + r.toString() + "\n";
	}

	return str;
    }
}

//*************************** OP NODES ***************************//

/*ADD NODE*/
class AddNode implements ExpressionNode {

    private ExpressionNode e1;
    private ExpressionNode e2;

    public AddNode(ExpressionNode e1, ExpressionNode e2){
	this.e1 = e1;
	this.e2 = e2;
    }

    public int evaluate (Robot robot) {
	return e1.evaluate(robot) + e2.evaluate(robot);
    }

    public String toString() {
	return "(" + e1.toString() + " PLUS " + e2.toString() + ")";
    }
}

/*SUB NODE*/
class SubNode implements ExpressionNode{

    private ExpressionNode e1;
    private ExpressionNode e2;

    public SubNode (ExpressionNode e1, ExpressionNode e2){
	this.e1 = e1;
	this.e2 = e2;
    }

    public int evaluate (Robot robot) {
	return e1.evaluate(robot) - e2.evaluate(robot);
    }

    public String toString() {
	return "(" + e1.toString() + " MINUS " + e2.toString() + ")";
    }
}

/*MUL NODE*/
class MulNode implements ExpressionNode{
    private ExpressionNode e1;
    private ExpressionNode e2;

    public MulNode(ExpressionNode e1, ExpressionNode e2){
	this.e1 = e1;
	this.e2 = e2;
    }

    public int evaluate (Robot robot) {
	return (e1.evaluate(robot) * e2.evaluate(robot));
    }

    public String toString() {
	return "(" + e1.toString() + " TIMES " + e2.toString() + ")";
    }
}

/*DIVIDE NODE*/
class DivNode implements ExpressionNode{
    private ExpressionNode e1;
    private ExpressionNode e2;

    public DivNode(ExpressionNode e1, ExpressionNode e2){
	this.e1 = e1;
	this.e2 = e2;
    }

    public int evaluate (Robot robot) {
	return (e1.evaluate(robot)/e2.evaluate(robot));
    }

    public String toString() {
	return "(" + e1.toString() + " DIVIDED BY " + e2.toString() + ")";
    }
}


//*************************** UNCATEGORISED NODES ***************************//

/*NUM NODE*/
class NumNode implements ExpressionNode {
    private int number;

    public NumNode (int num){
	this.number = num;
    }

    public int evaluate() {
	return this.number;
    }

    public int evaluate(Robot robot) {
	return number;
    }

    public String toString(){
	return this.number + "" ;
    }
}

/*VARIABLRE NODE*/
class VariableNode implements ExpressionNode{
    private String name;
    private ExpressionNode expression;


    public VariableNode(String name){
	this.name = name;
    }

    public int evaluate(Robot robot) {
	return Parser.variables.get(this.name);
    }

    public String toString() {
	return this.name;
    }
}









