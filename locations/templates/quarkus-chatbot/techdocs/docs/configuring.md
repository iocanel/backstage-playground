# Configuring 

## Using OpenAI

To use OpenAI in your chatbot, you need to create an account on the OpenAI website and get an API key. You can then use this key to authenticate your requests to the OpenAI API.


## Using the API Key in your application.properties

To use the OpenAI API key in your Quarkus application, you can add it to your `application.properties` file. 

```properties
quarkus.langchain4j.openai.api-key=<YOUR_API_KEY>
```

## Using the API Key as an Environment Variable

To avoid setting the API key directly in your code, you can use an environment variable to store it. This way, you can keep your API key secure and separate from your code.

The name of the environment variable should be `QUARKUS_LANGCHAIN4J_OPENAI_API_KEY`. 

