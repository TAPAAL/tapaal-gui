package dk.aau.cs.translations;

import dk.aau.cs.util.Tuple;

public interface ModelTranslator<TInputModel, TInputQuery, TOutputModel, TOutputQuery> {
	Tuple<TOutputModel, TOutputQuery> translate(TInputModel model, TInputQuery query) throws Exception;
	TranslationNamingScheme namingScheme();
}
