local key = KEYS[1]
local operations = redis.call('HGETALL', key)
if #operations > 0 then
    redis.call('DEL', key)
end
return operations