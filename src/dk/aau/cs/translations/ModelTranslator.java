package dk.aau.cs.translations;

public interface ModelTranslator<TInput, TOutput> {
	TOutput transformModel(TInput model) throws Exception;
}
