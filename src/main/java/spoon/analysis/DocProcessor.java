package spoon.analysis;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static spoon.reflect.declaration.ModifierKind.PROTECTED;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;

/**
 * Reports warnings for undocumented elements
 */
public class DocProcessor extends AbstractProcessor<CtElement> {
	// used in an assertion
	public final List<CtElement> undocumentedElements = new ArrayList<>();

	public void process(CtElement element) {
		if (element instanceof CtType || element instanceof CtField || element instanceof CtExecutable) {
			Set<ModifierKind> modifiers = ((CtModifiable) element).getModifiers();
			if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
				String docComment = element.getDocComment();
				if (docComment == null || docComment.equals("")) {
					System.out.println("undocumented element at " + element.getPosition());
					undocumentedElements.add(element);
				}
			}
		}
	}

}
