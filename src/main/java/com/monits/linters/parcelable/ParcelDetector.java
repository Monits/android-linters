package com.monits.linters.parcelable;

import static org.objectweb.asm.Opcodes.ASM5;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.ClassScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.monits.linters.parcelable.models.ParcelableField;
import com.monits.linters.parcelable.models.QueueManager;
import com.monits.linters.parcelable.visitors.ParcelClassVisitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author mpurita
 *
 *         Check if you write the variables of the parcelable object in the same
 *         order as you read. Also check if you forgot to write or read some
 *         variable.
 */

public class ParcelDetector extends Detector implements ClassScanner {
	private static final String MESSAGE_ERROR = "You are writing and reading in different"
			+ " ways or you forgot to read or write some variables";
	private final Queue<ParcelableField> writeQueue;
	private final Queue<ParcelableField> readQueue;

	@SuppressFBWarnings(value = "MISSING_FIELD_IN_TO_STRING" ,
			justification = "Variable used for local validation only")
	private boolean classLinted;

	/** The main issue discovered by this detector */
	public static final Issue MISSING_OR_OUT_OF_ORDER = Issue.create(
			"MissingOrOutOfOrder", //$NON-NLS-1$
			MESSAGE_ERROR, "If you write A and B, in this order, in the writeToParcel method you must "
					+ " read the variables in the same order (A and then B) and you have to"
					+ " read the same number of variables that you wrote", Category.CORRECTNESS, 8, Severity.ERROR,
			new Implementation(ParcelDetector.class, Scope.CLASS_FILE_SCOPE));

	/**
	 * Constructs a new {@link com.com.monits.linters.parcelable.checks.ParcelDetector}
	 * check
	 */
	public ParcelDetector() {
		this.writeQueue = new LinkedList<>();
		this.readQueue = new LinkedList<>();
	}

	@Nonnull
	@Override
	public List<String> getApplicableCallOwners() {
		return Arrays.asList("android/os/Parcel");
	}

	@Override
	public void afterCheckFile(@Nonnull final Context context) {
		while (!writeQueue.isEmpty() && !readQueue.isEmpty()) {
			final ParcelableField writeField = writeQueue.peek();
			final ParcelableField readField = readQueue.peek();
			if (writeField.equals(readField)) {
				writeQueue.remove(writeField);
				readQueue.remove(readField);
			} else {
				context.report(MISSING_OR_OUT_OF_ORDER, writeField.getLocation(), MESSAGE_ERROR);
				writeQueue.remove(writeField);
				readQueue.remove(writeField);
			}
		}
		reportMissingVariables(context, readQueue);
		reportMissingVariables(context, writeQueue);

		resetVariables();
	}

	private void reportMissingVariables(@Nonnull final Context context,
			@Nonnull final Queue<ParcelableField> queue) {
		for (final ParcelableField field : queue) {
			context.report(MISSING_OR_OUT_OF_ORDER, field.getLocation(), MESSAGE_ERROR);
		}
	}

	private void resetVariables() {
		writeQueue.clear();
		readQueue.clear();
		classLinted = false;
	}

	@Override
	public void checkCall(@Nonnull final ClassContext context, @Nonnull final ClassNode classNode,
			@Nonnull final MethodNode method, @Nonnull final MethodInsnNode call) {
		if (!classLinted) {
			try {
				final ClassReader cr = new ClassReader(new FileInputStream(context.file));
				final QueueManager<ParcelableField> queueManager = new QueueManager<>(writeQueue, readQueue);
				cr.accept(new ParcelClassVisitor(ASM5, classNode, context, queueManager, cr), 0);
				classLinted = true;
			} catch (final IOException e) {
				Logger.getLogger(ParcelDetector.class.getName()).log(Level.WARNING,
						"Error checking the file" + context.file, e);
			}
		}
	}

	@Override
	public String toString() {
		return "Parcelable linter that checks read and write order";
	}
}