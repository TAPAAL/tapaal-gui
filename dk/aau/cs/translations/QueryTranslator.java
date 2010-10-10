package dk.aau.cs.translations;

public interface QueryTranslator<TInput, TOutput> {
	TOutput transformQuery(TInput query) throws Exception;
}
