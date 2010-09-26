package dk.aau.cs.translations;

public interface ModelTransformer<TInput, TOutput> {
	TOutput transformModel(TInput model) throws Exception;
}
