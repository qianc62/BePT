package com.iise.shudi.exroru.dependency.sda;

import org.jbpt.petri.unfolding.CompletePrefixUnfolding;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;

import java.util.*;

public class Marking extends HashMap<Condition, Integer> {

    private static final long serialVersionUID = 4819455305876082860L;

    // associated net
    private ProperCompletePrefixUnfolding _cpu = null;
    private Event preVisEvent = null;
    private Event preInvEvent = null;
    private Set<Event> postEnabledEvents = new HashSet<>();
    private Set<Event> postDisabledEvents = new HashSet<>();

    public Marking() {
    }

    public Marking(ProperCompletePrefixUnfolding cpu) {
        if (cpu == null)
            throw new IllegalArgumentException(
                    "CompletePrefixUnfolding object expected but was NULL!");
        this._cpu = cpu;
    }

    public static Marking createMarking(ProperCompletePrefixUnfolding cpu) {
        Marking m;
        try {
            m = Marking.class.newInstance();
            m.setCompletePrefixUnfolding(cpu);
            return m;
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        } catch (InstantiationException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public Integer put(Condition c, Integer tokens) {
        if (c == null) {
            return 0;
        }
        if (!this._cpu.getConditions().contains(c)) {
            throw new IllegalArgumentException(
                    "Proposed condition is not part of the associated net!");
        }
        Integer result;
        if (tokens == null)
            result = super.remove(c);
        else {
            if (tokens <= 0) {
                result = super.remove(c);
            } else {
                result = super.put(c, tokens);
            }
        }
        return result == null ? 0 : result;
    }

//    public boolean isMarked(Condition condition) {
//        return this.get(condition) > 0;
//    }

//    public Collection<Condition> toMultiSet() {
//        Collection<Condition> result = new ArrayList<>();
//        for (Map.Entry<Condition, Integer> entry : this.entrySet()) {
//            for (int i = 0; i < entry.getValue(); i++) {
//                result.add(entry.getKey());
//            }
//        }
//        return result;
//    }

    public void fromMultiSet(Collection<Condition> conditions) {
        this.clear();
        for (Condition c : conditions) {
            if (!this._cpu.getConditions().contains(c)) {
                continue;
            }
            Integer tokens = this.get(c);
            if (tokens == null) {
                this.put(c, 1);
            } else {
                this.put(c, tokens + 1);
            }
        }
        this._cpu.getEvents().stream().filter(this::isEnabled)
                .forEach(this.postEnabledEvents::add);
    }

    public Integer remove(Condition condition) {
        return super.remove(condition);
    }

    public Integer get(Condition condition) {
        Integer i = super.get(condition);
        return i == null ? 0 : i;
    }

    public void clear() {
        super.clear();
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public Integer remove(Object condition) {
        return super.remove(condition);
    }

    public Integer get(Object c) {
        if (!(c instanceof Condition))
            return 0;
        Integer i = super.get(c);
        return i == null ? 0 : i;
    }

    public int size() {
        return super.size();
    }

    public Set<Map.Entry<Condition, Integer>> entrySet() {
        return super.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Marking)) {
            return false;
        }
        Marking that = (Marking) o;
        if (this.size() != that.size()) {
            return false;
        }

        for (Map.Entry<Condition, Integer> i : this.entrySet()) {
            Integer value = that.get(i.getKey());
            if (value == null) {
                return false;
            }
            if (!i.getValue().equals(value)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result -= this._cpu.hashCode();
        for (Condition c : this._cpu.getConditions()) {
            result += 17 * c.hashCode() * this.get(c);
        }
        result += (preVisEvent == null) ? 0 : 19 * preVisEvent.hashCode();

        return result;
    }

    public void setCompletePrefixUnfolding(ProperCompletePrefixUnfolding cpu) {
        this.clear();
        this._cpu = cpu;
    }

    public boolean isEnabled(Event e) {
        if (!this._cpu.getEvents().contains(e)) {
            return false;
        }

        for (Condition c : e.getPreConditions()) {
            if (this.get(c) == 0) {
                return false;
            }
        }
        return true;
    }

    public Set<Event> getEnabledEvents() {
        Set<Event> enabledEvents = new HashSet<>();
        this._cpu.getEvents().stream().filter(this::isEnabled)
                .forEach(enabledEvents::add);
        return enabledEvents;
    }

    /**
     * Fire e and update preEvent & postEvents
     */
    public boolean fire(Event e) {
        if (!this.isEnabled(e)) {
            return false;
        }

        if (e.getTransition().isSilent()) {
            this.preInvEvent = e;
        } else {
            this.preInvEvent = null;
            this.preVisEvent = e;
        }
        e.getPreConditions().stream()
                .forEach(c -> this.put(c, this.get(c) - 1));
        Set<Event> newEvents = new HashSet<>();
        for (Condition c : e.getPostConditions()) {
            if (c.isCutoffPost() && c.getPostE().isEmpty()) {
                c = c.getCorrespondingCondition();
            }
            this.put(c, this.get(c) + 1);
            newEvents.addAll(c.getPostE());
        }
        this.postEnabledEvents.clear();
        this.postDisabledEvents.clear();
        for (Event newEvent : newEvents) {
            if (this.isEnabled(newEvent)) {
                this.postEnabledEvents.add(newEvent);
            } else {
                this.postDisabledEvents.add(newEvent);
            }
        }

        return true;
    }

    /**
     * Only fire e without updating preEvent & postEvents
     */
    public boolean onlyFire(Event e) {
        if (!this.isEnabled(e)) {
            return false;
        }
        e.getPreConditions().stream()
                .forEach(c -> this.put(c, this.get(c) - 1));
        for (Condition c : e.getPostConditions()) {
            if (c.isCutoffPost() && c.getPostE().isEmpty()) {
                c = c.getCorrespondingCondition();
            }
            this.put(c, this.get(c) + 1);
        }
        return true;
    }

    public Marking clone() {
        Marking cloneMarking = (Marking) super.clone();
        cloneMarking._cpu = this._cpu;
        cloneMarking.preVisEvent = this.preVisEvent;
        cloneMarking.preInvEvent = this.preInvEvent;
        cloneMarking.postEnabledEvents = new HashSet<>(
                this.postEnabledEvents);
        cloneMarking.postDisabledEvents = new HashSet<>(
                this.postDisabledEvents);
        return cloneMarking;
    }

    public Event getPreVisEvent() {
        return preVisEvent;
    }

    public Event getPreInvEvent() {
        return preInvEvent;
    }

    public Set<Event> getPostEnabledEvents() {
        return postEnabledEvents;
    }

    public Set<Event> getPostDisabledEvents() {
        return postDisabledEvents;
    }

}
