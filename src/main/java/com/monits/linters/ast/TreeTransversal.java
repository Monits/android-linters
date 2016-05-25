/**
 *  Copyright 2010 - 2016 - Monits
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 *   file except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 *   ANY KIND, either express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package com.monits.linters.ast;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import lombok.ast.Block;
import lombok.ast.Cast;
import lombok.ast.Expression;
import lombok.ast.Identifier;
import lombok.ast.Node;
import lombok.ast.Select;
import lombok.ast.Statement;
import lombok.ast.This;
import lombok.ast.TypeBody;
import lombok.ast.TypeMember;
import lombok.ast.VariableDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import lombok.ast.VariableReference;

public final class TreeTransversal {

	private TreeTransversal() {
		throw new AssertionError("Utility cass can't be instantiated");
	}
	
	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T extends Node> T getClosestParent(@Nonnull final Node node, @Nonnull final Class<T> clazz) {
		Node n = node;
		while (n != null) {
			if (clazz.isAssignableFrom(n.getClass())) {
				return (T) n;
			}
			n = n.getParent();
		}
		
		throw new AssertionError("A node was passed not belonging to any " + clazz.getSimpleName());
	}
	
	public static boolean variableIsField(@Nonnull final VariableReference reference) {
		final VariableDefinitionEntry definitionEntry = getDefinitionForVariable(reference);
		
		if (definitionEntry != null) {
			return variableIsField(definitionEntry);
		}
		
		// Not found!!
		return false;
	}
	
	public static boolean variableIsField(@Nonnull final VariableDefinitionEntry definition) {
		if (definition.upUpIfFieldToTypeDeclaration() != null) {
			return true;
		}
			
		// Not found!!
		return false;
	}
	
	@CheckForNull
	public static VariableDefinitionEntry getDefinitionForVariable(@Nonnull final VariableReference reference) {
		return getDefinitionForVariable(reference.astIdentifier());
	}
	
	@CheckForNull
	public static VariableDefinitionEntry getDefinitionForVariable(@Nonnull final Identifier identifier) {
		final String varName = identifier.astValue();
		Node block = identifier.getParent();
		do {
			if (block instanceof TypeBody) {
				// We reached the type definition, it should be here, but check just in case
				for (final TypeMember tm : ((TypeBody) block).astMembers()) {
					if (tm instanceof VariableDeclaration) {
						final VariableDeclaration vd = (VariableDeclaration) tm;
						for (final VariableDefinitionEntry var : vd.astDefinition().astVariables()) {
							if (varName.equals(var.astName().astValue())) {
								return var;
							}
						}
					}
				}
			} else if (block instanceof Block) {
				// Check if there are any VariableDefinitions for the variable in a parent block
				for (final Statement stmt : ((Block) block).astContents()) {
					if (stmt instanceof VariableDefinition) {
						final VariableDefinition def = (VariableDefinition) stmt;
						for (final VariableDefinitionEntry var : def.astVariables()) {
							if (varName.equals(var.astName().astValue())) {
								return var;
							}
						}
					}
				}
			}
			block = block.getParent();
		} while (block != null);
		
		// Should never happen!
		return null;
	}
	
	@CheckForNull
	public static VariableDefinitionEntry getDefinitionForWrappedVariable(@Nonnull final Expression exp) {
		if (exp instanceof VariableReference) {
			return getDefinitionForVariable((VariableReference) exp);
		} else if (exp instanceof Cast) {
			return getDefinitionForWrappedVariable(((Cast) exp).astOperand());
		} else if (exp instanceof Select) {
			final Select select = (Select) exp;
			if (select.astOperand() instanceof This) {
				return getDefinitionForVariable(select.astIdentifier());
			}
		}
		
		return null;
	}
}
