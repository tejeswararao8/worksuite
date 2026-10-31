const http = require('http');
const fs = require('fs');

const ADMIN_TOKEN = require('./admin_token.json').access_token;
const BASE = 'localhost';
const PORT = 8080;
const REALM = 'vve-platform';
const USER_ID = '9e294114-ce44-4883-9159-f91b5b107533';

const ROLES = [
  'EMPLOYEE_READ','EMPLOYEE_WRITE',
  'DEPARTMENT_READ','DEPARTMENT_WRITE',
  'BRANCH_READ','BRANCH_WRITE',
  'DESIGNATION_READ','DESIGNATION_WRITE',
  'TEAM_READ','TEAM_WRITE',
  'ASSET_READ','ASSET_WRITE',
  'TRANSFER_READ','TRANSFER_WRITE',
  'PROMOTION_READ','PROMOTION_WRITE',
  'PROBATION_READ','PROBATION_WRITE',
  'ONBOARDING_READ','ONBOARDING_WRITE',
  'OFFBOARDING_READ','OFFBOARDING_WRITE',
  'SKILL_READ','SKILL_WRITE',
  'HR_REPORT_EXPORT',
  'HR_ADMIN','FINANCE_ADMIN',
  'ROLE_READ','ROLE_WRITE',
  'USER_READ','USER_WRITE',
  'AUDIT_READ','NOTIFICATION_READ'
];

function req(method, path, body) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : null;
    const options = {
      hostname: BASE, port: PORT, path, method,
      headers: {
        'Authorization': `Bearer ${ADMIN_TOKEN}`,
        'Content-Type': 'application/json',
        ...(data ? { 'Content-Length': Buffer.byteLength(data) } : {})
      }
    };
    const r = http.request(options, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve({ status: res.statusCode, body: d }));
    });
    r.on('error', reject);
    if (data) r.write(data);
    r.end();
  });
}

async function main() {
  // 1. Create all roles
  for (const role of ROLES) {
    const res = await req('POST', `/admin/realms/${REALM}/roles`, { name: role });
    console.log(`Create role ${role}: ${res.status}`);
  }

  // 2. Get all role IDs
  const rolesRes = await req('GET', `/admin/realms/${REALM}/roles`);
  const allRoles = JSON.parse(rolesRes.body);
  const toAssign = allRoles.filter(r => ROLES.includes(r.name)).map(r => ({ id: r.id, name: r.name }));
  console.log(`Found ${toAssign.length} roles to assign`);

  // 3. Assign all roles to testuser
  const assignRes = await req('POST', `/admin/realms/${REALM}/users/${USER_ID}/role-mappings/realm`, toAssign);
  console.log(`Assign roles to testuser: ${assignRes.status}`);
}

main().catch(console.error);
