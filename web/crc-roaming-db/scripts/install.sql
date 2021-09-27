-- journal_change id is the id from JournalObjectTypeEnum.class
DROP TRIGGER IF EXISTS upd_countries_trigger on public.countries;
CREATE TRIGGER upd_countries_trigger
	AFTER INSERT OR UPDATE ON public.countries
	FOR EACH ROW EXECUTE PROCEDURE journal_change(0);

DROP TRIGGER IF EXISTS upd_operators_trigger on public.operators;
CREATE TRIGGER upd_operators_trigger
	AFTER INSERT OR UPDATE ON public.operators
	FOR EACH ROW EXECUTE PROCEDURE journal_change(1);

DROP TRIGGER IF EXISTS upd_high_risk_zones_trigger on public.high_risk_zones;
CREATE TRIGGER upd_high_risk_zones_trigger
	AFTER INSERT OR UPDATE ON public.high_risk_zones
	FOR EACH ROW EXECUTE PROCEDURE journal_change(2);
	
DROP TRIGGER IF EXISTS upd_users_trigger on web_security.users;
CREATE TRIGGER upd_users_trigger
	AFTER INSERT OR UPDATE ON web_security.users
	FOR EACH ROW EXECUTE PROCEDURE journal_change(3);
	
DROP TRIGGER IF EXISTS upd_roles_trigger on web_security.authorities;
CREATE TRIGGER upd_roles_trigger
	AFTER INSERT OR UPDATE ON web_security.authorities
	FOR EACH ROW EXECUTE PROCEDURE journal_change(4);
	
CREATE OR REPLACE FUNCTION journal_change() RETURNS TRIGGER AS $journal_change$
	DECLARE
		old_jsonb jsonb;
		new_jsonb jsonb;
		updated_by integer;
		operation integer;
	BEGIN
		-- if new or updated
		if (old is null) then
			old_jsonb := '{}';
			updated_by := new.created_by;
			operation := 0;
		else
			old_jsonb := jsonb_strip_nulls(to_jsonb(old)) - 'created_by' - 'created_at' - 'updated_by' - 'updated_at' - 'deleted_by' - 'deleted_at' - 'password' - 'vrsn';
			updated_by := new.updated_by;
			operation := 1;
		end if;
	
		-- if updated or deleted
		if (new.deleted_by is not null) then
			new_jsonb := '{}';
			updated_by := new.deleted_by;
			operation := 2;
		else
			new_jsonb := jsonb_strip_nulls(to_jsonb(new)) - 'created_by' - 'created_at' - 'updated_by' - 'updated_at' - 'deleted_by' - 'deleted_at'  - 'password' - 'vrsn';
		end if;
	
		if (old_jsonb is distinct from new_jsonb) then
			insert into web.journal_changes(initial_state, new_state, ts, record_id, editor_id, object_type, operation_type)
			values(old_jsonb, new_jsonb, now(), new.id, updated_by, cast(TG_ARGV[0] as integer), operation);
	    end if;
	
	    return new;
	END;
$journal_change$ LANGUAGE plpgsql;

DROP FUNCTION public.count_roamings_monthly_gr(integer, integer, integer, integer, integer, integer, integer);
CREATE OR REPLACE FUNCTION public.count_roamings_monthly_gr(frommonth integer, fromyear integer, tomonth integer, toyear integer, countryid integer, operatorid integer, regionid integer)
 RETURNS TABLE(month text, count bigint)
 LANGUAGE plpgsql
AS $function$
	DECLARE
		fd date;
		td date;
	BEGIN
		fd := make_date(fromYear, fromMonth, 1);
		td := (make_date(toYear, toMonth, 1) + interval '1 month' - interval '1 day')::date;
		return query
			select to_char(event_ts, 'YYYY-MM') as "month", count(*) as "count"
			from public.reported_roamings rr
			join public.operators o on o.id = rr.operator_id 
			where
				rr.hidden is false
				and rr.event_ts between fd and td
				and case when countryId > 0 then o.country_id = countryId else true end
				and case when operatorId > 0 then rr.operator_id = operatorId else true end
				and case when regionId > 0 then rr.bg_region_id = regionId else true end
			group by "month"
			order by "month" asc;
	END;
$function$
;


DROP FUNCTION public.count_blockings_monthly_gr(integer, integer, integer, integer, integer, integer);
CREATE OR REPLACE FUNCTION public.count_blockings_monthly_gr(frommonth integer, fromyear integer, tomonth integer, toyear integer, countryid integer, operatorid integer)
 RETURNS TABLE(month text, count bigint)
 LANGUAGE plpgsql
AS $function$
	DECLARE
		fd date;
		td date;
	BEGIN
		fd := make_date(fromYear, fromMonth, 1);
		td := (make_date(toYear, toMonth, 1) + interval '1 month' - interval '1 day')::date;
		return query
			select to_char(event_ts, 'YYYY-MM') as "month", count(*) as "count"
			from public.reported_blockings rb
			join public.operators o on o.id = rb.operator_id 
			where
				rb.event_ts between fd and td
				and case when countryId > 0 then o.country_id = countryId else true end
				and case when operatorId > 0 then rb.operator_id = operatorId else true end
			group by "month"
			order by "month";
	END;
$function$
;
