package tests.languages.c.parseTreeToAST;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import antlr.FunctionParser.StatementsContext;
import ast.ASTNode;
import ast.Comment;
import ast.c.expressions.CallExpression;
import ast.c.statements.blockstarters.IfStatement;
import ast.declarations.ClassDefStatement;
import ast.declarations.IdentifierDecl;
import ast.expressions.Argument;
import ast.expressions.ArgumentList;
import ast.expressions.AssignmentExpression;
import ast.logical.statements.Condition;
import ast.logical.statements.BlockStarter;
import ast.logical.statements.CompoundStatement;
import ast.statements.ExpressionStatement;
import ast.statements.IdentifierDeclStatement;
import ast.statements.blockstarters.ForStatement;

public class CodeNestingTest {

	@Test
	public void testLineNumbers() {
		String input = "if(foo)\nbar();\nfoo()\n";
		StatementsContext ctx = (StatementsContext) FunctionContentTestUtil.parse(input);
		assert (ctx.start.getLine() == 1);
		assert (ctx.stop.getLine() == 3);
	}

	@Test
	public void emptyContent() {
		String input = "";
		CompoundStatement item = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		assert (item.getStatements().size() == 0);
	}

	@Test
	public void compoundWithoutBlockStart() {
		String input = "bar(); {}";
		CompoundStatement item = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		assertTrue(item.getStatements().size() == 2);
	}

	@Test
	public void assignmentInCondition() {
		String input = "if(foo = bar){}";
		CompoundStatement item = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		BlockStarter starter = (BlockStarter) item.getStatements().get(0);
		AssignmentExpression condition = (AssignmentExpression) ((Condition) starter.getCondition()).getExpression();
		assertTrue(condition.getEscapedCodeStr().equals("foo = bar"));
	}

	@Test
	public void whileInElse() {
		String input = "if(foo){bar();}else{ while(foo1){ if(bar2){} } }";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		IfStatement ifItem = (IfStatement) contentItem.getStatements().get(0);
	}

	@Test
	public void complexIfElseNesting() {
		String input = "if (A){ if (B){ } if (C){ } } else { }";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		IfStatement ifItem = (IfStatement) contentItem.getStatements().get(0);
		assertTrue(ifItem.getElseNode() != null);
	}

	@Test
	public void testFor() {
		String input = "for(i = 0; i < 10; i++){}";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		ForStatement forItem = (ForStatement) contentItem.getStatements().get(0);

		String condExprString = ((Condition) forItem.getCondition()).getExpression().getEscapedCodeStr();

		assertTrue(condExprString.equals("i < 10"));

	}

	@Test
	public void testDeclInFor() {
		String input = "for(int i = 0; i < 10; i++){}";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		ForStatement forItem = (ForStatement) contentItem.getStatements().get(0);

		String condExprString = ((Condition) forItem.getCondition()).getExpression().getEscapedCodeStr();
		assertTrue(condExprString.equals("i < 10"));

	}

	@Test
	public void testVarDeclName() {
		String input = "int x = 2*y;";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		IdentifierDeclStatement declStatement = (IdentifierDeclStatement) contentItem.getStatements().get(0);
		IdentifierDecl decl = (IdentifierDecl) declStatement.getChild(0);
		assertTrue(decl.getName().getEscapedCodeStr().equals("x"));
	}

	@Test
	public void testVarDeclType() {
		String input = "int x = 2*y;";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		IdentifierDeclStatement declStatement = (IdentifierDeclStatement) contentItem.getStatements().get(0);
		IdentifierDecl decl = (IdentifierDecl) declStatement.getChild(0);

		assertEquals("int", decl.getType().getEscapedCodeStr());
	}

	@Test
	public void testAssignment() {
		String input = "const char *m = \"Usage: untar [-tvx] [-f file] [file]\\n\";";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		IdentifierDeclStatement declStatement = (IdentifierDeclStatement) contentItem.getStatements().get(0);
		IdentifierDecl decl = (IdentifierDecl) declStatement.getChild(0);

		AssignmentExpression assign = (AssignmentExpression) decl.getChild(decl.getChildCount() - 1);
		assertTrue(assign.getLeft().getEscapedCodeStr().equals("m"));
		assertTrue(assign.getRight().getEscapedCodeStr().equals("\"Usage: untar [-tvx] [-f file] [file]\\n\""));
	}

	@Test
	public void testDeclRightAfterStruct() {
		String input = "struct foo{ int x; } foo;";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		assertTrue(contentItem.getChildCount() == 1);
		ClassDefStatement classDef = (ClassDefStatement) contentItem.getChild(0);
		assertTrue(classDef.getChildCount() == 2);
		IdentifierDecl decl = (IdentifierDecl) classDef.getChild(1);
		assertTrue(decl.getName().getEscapedCodeStr().equals("foo"));
	}

	@Test
	public void testCall() {
		String input = "foo(x);";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		ExpressionStatement stmt = (ExpressionStatement) contentItem.getStatements().get(0);
		CallExpression expr = (CallExpression) stmt.getChild(0);
		assertEquals("foo", expr.getTargetFunc().getEscapedCodeStr());
		ArgumentList argList = (ArgumentList) expr.getChild(1);
		Argument arg = (Argument) argList.getChild(0);
		assertEquals("x", arg.getEscapedCodeStr());
	}

	@Test
	public void testCallWithTwoArguments() {
		String input = "foo(x,y);";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);
		ExpressionStatement stmt = (ExpressionStatement) contentItem.getStatements().get(0);
		CallExpression expr = (CallExpression) stmt.getChild(0);
		assertTrue(expr.getTargetFunc().getEscapedCodeStr().equals("foo"));
	}
	
	
	@Test
	public void commentInsideFunction() {
		String input = "/*Comment inside function */ int i; ";
		CompoundStatement contentItem = (CompoundStatement) FunctionContentTestUtil.parseAndWalk(input);		
		Comment codeItem = (Comment) contentItem.getStatements().get(1);
		assertEquals("Comment", codeItem.getTypeAsString());
		assertEquals("/*Comment inside function */", codeItem.getEscapedCodeStr());
		assertEquals("IdentifierDeclStatement", codeItem.getCommentee().getTypeAsString());
	}

}
