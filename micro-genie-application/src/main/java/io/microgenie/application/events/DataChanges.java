package io.microgenie.application.events;

import java.util.Map;

import org.assertj.core.util.Maps;

public class DataChanges {
	
	private Map<String, Object> submitted = Maps.newHashMap();
	
	private final Map<String, Object> added = Maps.newHashMap();
	private final Map<String, Object> removed = Maps.newHashMap();
	private final Map<String, Object> modified = Maps.newHashMap();
	
	
	public DataChanges(final Map<String, Object> submitted){
		this.submitted.putAll(submitted);
	}
	public Map<String, Object> getAdded() {
		return added;
	}
	public void added(Map<String, Object> added){
		this.added.putAll(added);
	}
	public Map<String, Object> getRemoved() {
		return removed;
	}
	public void removed(Map<String, Object> removed){
		this.removed.putAll(removed);
	}
	public Map<String, Object> getModified() {
		return modified;
	}
	public void modified(Map<String, Object> modified){
		this.modified.putAll(modified);
	}
	public Map<String, Object> getSubmitted() {
		return submitted;
	}
	public void submitted(Map<String, Object> submitted){
		this.submitted.putAll(submitted);
	}
	public static DataChanges create(final Map<String, Object> submitted, final Map<String, Object> added, final Map<String, Object> removed, final Map<String, Object> modified){
		final DataChanges changes = new DataChanges(submitted);
		if(added!=null){
			changes.added(added);
		}
		if(removed!=null){
			changes.removed(removed);
		}
		if(modified!=null){
			changes.modified(modified);
		}
		return changes;
	}
	
	
	@SuppressWarnings("unchecked")
	public static DataChanges from(EventData actualData) {

		final Map<String,Object> dataChangeMaps = actualData.getData();
		final Map<String, Object> submitted = (Map<String, Object>)dataChangeMaps.get("submitted");
		final Map<String, Object> added = (Map<String, Object>)dataChangeMaps.get("added");
		final Map<String, Object> removed = (Map<String, Object>)dataChangeMaps.get("removed");
		final Map<String, Object> modified = (Map<String, Object>)dataChangeMaps.get("modified");
		return DataChanges.create(submitted, added, removed, modified);
	}
}
