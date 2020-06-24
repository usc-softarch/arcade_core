package edu.usc.softarch.arcade.antipattern.detection.interfacebased.pmd;

import java.util.concurrent.atomic.AtomicLong;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class CountRule extends AbstractJavaRule {

       private static final String COUNT = "count";

       @Override
       public void start(RuleContext ctx) {
               ctx.setAttribute(COUNT, new AtomicLong());
               super.start(ctx);
       }

       @Override
       public Object visit(ASTExpression node, Object data) {
               // How many Expression nodes are there in all files parsed!  I must know!
               RuleContext ctx = (RuleContext)data;
               AtomicLong total = (AtomicLong)ctx.getAttribute(COUNT);
               total.incrementAndGet();
               return super.visit(node, data);
       }

       @Override
       public void end(RuleContext ctx) {
               AtomicLong total = (AtomicLong)ctx.getAttribute(COUNT);
               addViolation(ctx, null, new Object[] { total });
               ctx.removeAttribute(COUNT);
               super.start(ctx);
       }
}

