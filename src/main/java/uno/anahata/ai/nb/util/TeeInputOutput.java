package uno.anahata.ai.nb.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

public class TeeInputOutput implements InputOutput {
    private static final Logger LOG = Logger.getLogger(TeeInputOutput.class.getName());

    private final InputOutput delegate;
    private final StringWriter capturedOutWriter = new StringWriter();
    private final StringWriter capturedErrWriter = new StringWriter();
    private final OutputWriter teeOut;
    private final OutputWriter teeErr;

    public TeeInputOutput(InputOutput delegate) {
        LOG.info("TeeInputOutput: Constructor called");
        this.delegate = delegate;
        this.teeOut = new TeeOutputWriter(delegate.getOut(), capturedOutWriter);
        this.teeErr = new TeeOutputWriter(delegate.getErr(), capturedErrWriter);
    }

    public String getCapturedOutput() {
        LOG.info("TeeInputOutput: getCapturedOutput called");
        return capturedOutWriter.toString();
    }

    public String getCapturedError() {
        LOG.info("TeeInputOutput: getCapturedError called");
        return capturedErrWriter.toString();
    }

    @Override
    public OutputWriter getOut() {
        LOG.info("TeeInputOutput: getOut called");
        return teeOut;
    }

    @Override
    public OutputWriter getErr() {
        LOG.info("TeeInputOutput: getErr called");
        return teeErr;
    }

    @Override
    public Reader getIn() {
        LOG.info("TeeInputOutput: getIn called");
        return new StringReader(getCapturedOutput());
    }

    @Override
    public void closeInputOutput() {
        LOG.info("TeeInputOutput: closeInputOutput called");
        teeOut.close();
        teeErr.close();
        delegate.closeInputOutput();
    }

    @Override
    public boolean isClosed() {
        LOG.info("TeeInputOutput: isClosed called");
        return delegate.isClosed();
    }

    @Override
    public Reader flushReader() {
        LOG.info("TeeInputOutput: flushReader called");
        return delegate.flushReader();
    }

    @Override
    public void select() {
        LOG.info("TeeInputOutput: select() called");
        delegate.select();
    }

    @Override
    public boolean isErrSeparated() {
        LOG.info("TeeInputOutput: isErrSeparated called");
        return delegate.isErrSeparated();
    }

    @Override
    public void setErrSeparated(boolean value) {
        LOG.log(Level.INFO, "TeeInputOutput: setErrSeparated called with value: {0}", value);
        delegate.setErrSeparated(value);
    }

    @Override
    public boolean isFocusTaken() {
        LOG.info("TeeInputOutput: isFocusTaken called");
        return delegate.isFocusTaken();
    }

    @Override
    public void setFocusTaken(boolean value) {
        LOG.log(Level.INFO, "TeeInputOutput: setFocusTaken called with value: {0}", value);
        delegate.setFocusTaken(value);
    }

    @Override
    public void setOutputVisible(boolean value) {
        LOG.log(Level.INFO, "TeeInputOutput: setOutputVisible called with value: {0}", value);
        delegate.setOutputVisible(value);
    }

    @Override
    public void setErrVisible(boolean value) {
        LOG.log(Level.INFO, "TeeInputOutput: setErrVisible called with value: {0}", value);
        delegate.setErrVisible(value);
    }

    @Override
    public void setInputVisible(boolean value) {
        LOG.log(Level.INFO, "TeeInputOutput: setInputVisible called with value: {0}", value);
        delegate.setInputVisible(value);
    }

    private static class TeeOutputWriter extends OutputWriter {
        private static final Logger LOG = Logger.getLogger(TeeOutputWriter.class.getName());
        private final Writer w1;
        private final Writer w2;
        private volatile boolean errorOccurred = false;

        public TeeOutputWriter(Writer w1, Writer w2) {
            super(w1);
            this.w1 = w1;
            this.w2 = w2;
            LOG.info("TeeOutputWriter: Constructor called");
        }

        private void handleException(IOException e) {
            if (!errorOccurred) {
                errorOccurred = true;
                LOG.log(Level.WARNING, "IOException in TeeOutputWriter. Further exceptions will be suppressed.", e);
            }
        }

        @Override
        public void write(int c) {
            try { w1.write(c); w2.write(c); } catch (IOException e) { handleException(e); }
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            try { w1.write(cbuf, off, len); w2.write(cbuf, off, len); } catch (IOException e) { handleException(e); }
        }

        @Override
        public void write(String s, int off, int len) {
            try { w1.write(s, off, len); w2.write(s, off, len); } catch (IOException e) { handleException(e); }
        }

        @Override
        public void flush() {
            LOG.info("TeeOutputWriter: flush called");
            try { w1.flush(); w2.flush(); } catch (IOException e) { handleException(e); }
        }

        @Override
        public void close() {
            LOG.info("TeeOutputWriter: close called");
            try { w1.close(); w2.close(); } catch (IOException e) { handleException(e); }
        }

        @Override
        public void reset() throws IOException {
            LOG.info("TeeOutputWriter: reset called");
            if (w1 instanceof OutputWriter) {
                ((OutputWriter) w1).reset();
            }
        }

        @Override
        public void println(String s, OutputListener l) {
            try {
                if (w1 instanceof OutputWriter) {
                    ((OutputWriter) w1).println(s, l);
                } else {
                    w1.write(s);
                    w1.write(System.lineSeparator());
                }
                w2.write(s);
                w2.write(System.lineSeparator());
            } catch (IOException e) {
                handleException(e);
            }
        }
    }
}