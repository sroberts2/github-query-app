package companieshouse.gov.uk.githubapi.util;

import java.util.Iterator;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SequenceAnswer<T> implements Answer<T> {

    private final Iterator<T> resultIterator;
    private final T last;

    public SequenceAnswer(final Iterator<T> resultIterator, final T last) {
        this.resultIterator = resultIterator;
        this.last = last;
    }



    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        if (resultIterator.hasNext()) {
            return resultIterator.next();
        }
        return last;
    }
    
}
