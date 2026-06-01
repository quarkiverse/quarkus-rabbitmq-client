package io.quarkiverse.rabbitmqclient.deployment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.quarkus.gizmo.Gizmo;

class FixAmqpClientClassLoaderClassVisitor extends ClassVisitor {
    public FixAmqpClientClassLoaderClassVisitor(ClassVisitor classVisitor) {
        super(Gizmo.ASM_API_VERSION, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (name.equals("loadResource") && descriptor.equals("(Ljava/lang/String;)Ljava/io/InputStream;")) {
            fixLoadResource(mv);
            // return null prevents original method generation
            return null;
        }
        return mv;
    }

    private void fixLoadResource(MethodVisitor methodVisitor) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitLdcInsn("classpath:");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
        Label label0 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitLdcInsn("classpath:");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;", false);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 1);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitLdcInsn("/");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
        Label label1 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label1);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;", false);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 1);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getContextClassLoader",
                "()Ljava/lang/ClassLoader;", false);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ClassLoader", "getResourceAsStream",
                "(Ljava/lang/String;)Ljava/io/InputStream;", false);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        methodVisitor.visitLabel(label0);
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/io/FileInputStream");
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/io/FileInputStream", "<init>", "(Ljava/lang/String;)V",
                false);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    // the method implementation as reference for ASM listed above
    private static InputStream loadResource(
            String location) throws FileNotFoundException {

        if (location.startsWith("classpath:")) {
            String p = location.substring("classpath:".length());
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            return Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(p);
        } else {
            return new FileInputStream(location);
        }
    }
}
