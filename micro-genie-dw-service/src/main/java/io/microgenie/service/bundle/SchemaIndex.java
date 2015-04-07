package io.microgenie.service.bundle;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/***
 * 
 * @author shawn
 */
class SchemaIndex {
	
	
	/***
	 * Create the index page for published JSON Schemas
	 * @param schemas
	 * @return
	 */
	public StringBuilder createIndex(final Map<String, Set<SchemaItem>> schemas){
		final StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		builder.append("<head>");
		builder.append("</head>");
		builder.append("<body>");
		
		/** Create The Service Indexes **/
		for(Entry<String, Set<SchemaItem>> entry : schemas.entrySet()){
			builder.append("<br />");
			builder.append("<b>").append(entry.getKey()).append("</b>");
			builder.append("<br />");
			builder.append("<ul>");
			/** build out links **/
			final Set<SchemaItem> nodes = entry.getValue();
			for(SchemaItem item : nodes){
				builder.append("<li><a href=\"");
				builder.append(item.link);
				builder.append("\">");
				builder.append(item.text);
				builder.append("</a></li>");
			}
			builder.append("</ul>");
			builder.append("<br />");	
		}
		builder.append("</body>");
		builder.append("</html>");	
		return builder;
	}
	
	
	
	/**
	 * @author shawn
	 */
	static class SchemaItem{
		private final String text;
		private final String link;
		public SchemaItem(final String text, final String link){
			this.text = text;
			this.link = link;
		}
		public String getText() {
			return text;
		}
		public String getLink() {
			return link;
		}
		public static SchemaItem create(final String text, final String link){
			return new SchemaItem(text, link);
		}
	}
}
