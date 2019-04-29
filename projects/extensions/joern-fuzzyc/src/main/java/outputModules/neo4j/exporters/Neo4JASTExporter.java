package outputModules.neo4j.exporters;

import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import ast.ASTNode;
import databaseNodes.ASTDatabaseNode;
import databaseNodes.EdgeTypes;
import databaseNodes.NodeKeys;
import neo4j.batchInserter.GraphNodeStore;
import neo4j.batchInserter.Neo4JBatchInserter;
import outputModules.common.ASTExporter;

public class Neo4JASTExporter extends ASTExporter {
	GraphNodeStore nodeStore;

	public Neo4JASTExporter(GraphNodeStore aNodeStore) {
		nodeStore = aNodeStore;
	}


	@Override
	protected void addASTNode(ASTDatabaseNode astDatabaseNode) {
		Map<String, Object> properties = astDatabaseNode.createProperties();
		properties.put(NodeKeys.FUNCTION_ID, nodeStore.getIdForObject(currentFunction));
		nodeStore.addNeo4jNode(astDatabaseNode, properties);

		nodeStore.indexNode(astDatabaseNode, properties);
		astDatabaseNode.setNodeId(nodeStore.getIdForObject(astDatabaseNode));
	}

	@Override
	protected void addASTLink(ASTNode parent, ASTNode child) {
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.IS_AST_PARENT);

		long parentId = nodeStore.getIdForObject(parent);
		long childId = nodeStore.getIdForObject(child);
		Map<String, Object> properties = null;

		Neo4JBatchInserter.addRelationship(parentId, childId, rel, properties);
	}
	
	/**
	 * Link the given preStatementDatabaseNode (parentNodeID) with its block content (childNodeID)
	 */
	@Override
	protected void drawVariabilityEdge(long parentNodeID, long childNodeID) {
		RelationshipType rel = DynamicRelationshipType.withName(EdgeTypes.VARIABILITY);
		Neo4JBatchInserter.addRelationship(parentNodeID, childNodeID, rel, null);
	}

}
