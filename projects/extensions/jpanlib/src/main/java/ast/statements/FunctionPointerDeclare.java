package ast.statements;

import ast.logical.statements.Statement;
import ast.walking.ASTNodeVisitor;

public class FunctionPointerDeclare extends Statement {

	public void accept(ASTNodeVisitor visitor) {
		visitor.visit(this);
	}

}
