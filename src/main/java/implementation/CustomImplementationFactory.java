package implementation;

import formatter.Formatter;
import formatter.util.FormatterUtil;
import interpreter.*;
import lexer.Lexer;
import lexer.util.LexerUtil;
import linter.Linter;
import linter.util.LinterUtil;
import parser.Parser;
import java.io.IOException;
import static common.util.SegmentUtilsKt.segmentsBySemicolon;
import static common.util.SegmentUtilsKt.segmentsBySemicolonPreserveWhitespace;

public class CustomImplementationFactory implements PrintScriptFactory{

    /*
     your PrintScript implementation should be returned here.
     make sure to ADAPT your implementation to PrintScriptInterpreter interface.
     Dummy impl: return (src, version, emitter, handler, provider) -> { };
     */
    @Override
    public PrintScriptInterpreter interpreter() {
       return (src, version, emitter, handler, provider) -> {
           var lexer = LexerUtil.Companion.createLexer(version);
           var parser = new Parser();
           var interpreter = new Interpreter(provider::input);
           try {
               var iterator = segmentsBySemicolon(src).iterator();
               while (iterator.hasNext()) {
                   String segment = iterator.next();
                   var tokens = lexer.lex(segment);
                   var ast = parser.parse(tokens);
                   var lines = interpreter.interpret(ast);

                   for (var line : lines) {
                       emitter.print(line);
                   }
               }
           } catch (OutOfMemoryError oom) {
               handler.reportError("Java heap space");
           } catch (Throwable t) {
               handler.reportError(t.getMessage());
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
            Lexer lexer = LexerUtil.Companion.createLexer(version);
            byte[] stringBytes = new byte[0]; // read all bytes into a byte array
            try {
                stringBytes = config.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String string = new String(stringBytes); // decodes stringBytes into a String
            Formatter formatter = FormatterUtil.Companion.createFormatter(string, version);
            var iterator = segmentsBySemicolonPreserveWhitespace(src).iterator();
            String formatterText = "";
            while (iterator.hasNext()) {
                String segment = iterator.next();
                var tokens = lexer.lex(segment);
                formatterText = formatterText + formatter.format(tokens);
            }
            try {
                writer.write(formatterText);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /*
     your PrintScript linter should be returned here.
     make sure to ADAPT your linter to PrintScriptLinter interface.
     Dummy impl: return (src, version, config, writer) -> { };
     */
    @Override
    public PrintScriptLinter linter() {
        return (src, version, config, handler) -> {
            Lexer lexer = LexerUtil.Companion.createLexer(version);
            byte[] stringBytes = new byte[0]; // read all bytes into a byte array
            try {
                stringBytes = config.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String string = new String(stringBytes);
            Linter linter = LinterUtil.Companion.createLinter(string, version);
            var iterator = segmentsBySemicolonPreserveWhitespace(src).iterator();
            while (iterator.hasNext()) {
                String segment = iterator.next();
                try {
                    var tokens = lexer.lex(segment);
                    var errors = linter.lint(tokens);
                    for (var error : errors) {
                        handler.reportError(error.getMessage());
                    }
                } catch (Throwable t) {
                    handler.reportError(t.getMessage());
                }
            }
        };
    }
}
