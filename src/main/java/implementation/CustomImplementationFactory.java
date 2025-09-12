package implementation;

import formatter.Formatter;
import interpreter.*;
import lexer.util.LexerUtil;
import parser.Parser;

import static common.util.SegmentUtilsKt.segmentsBySemicolon;

public class CustomImplementationFactory implements PrintScriptFactory{

    /*
     your PrintScript implementation should be returned here.
     make sure to ADAPT your implementation to PrintScriptInterpreter interface.
     Dummy impl: return (src, version, emitter, handler) -> { };
     */
    @Override
    public PrintScriptInterpreter interpreter() {
       return (src, version, emitter, handler, provider) -> {
           var lexer = LexerUtil.Companion.createLexer(version);
           var parser = new Parser();
           var interpreter = new Interpreter();

           var iterator = segmentsBySemicolon(src).iterator();
           while (iterator.hasNext()) {
               String segment = iterator.next();
               try {
                   var tokens = lexer.lex(segment);
                   var ast = parser.parse(tokens);
                   var lines = interpreter.interpret(ast);

                   for (var line : lines) {
                       emitter.print(line);
                   }
               } catch (Throwable t) {
                   handler.reportError(t.getMessage());
               }
           }
       };
    }

    /*
     your PrintScript formatter should be returned here.
     make sure to ADAPT your formatter to PrintScriptFormatter interface.
     Dummy impl: return (src, version, config, writer) -> { };
     */
    @Override
    public PrintScriptFormatter formatter() {
        return (src, version, config, writer) -> {
            throw new NotImplementedException("Needs implementation"); // TODO: implement
        };
    }

    @Override
    public PrintScriptLinter linter() {
        // your PrintScript linter should be returned here.
        // make sure to ADAPT your linter to PrintScriptLinter interface.
        throw new NotImplementedException("Needs implementation"); // TODO: implement
    }
}